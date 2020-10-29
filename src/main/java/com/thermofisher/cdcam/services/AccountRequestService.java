package com.thermofisher.cdcam.services;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.enums.cdc.Events;
import com.thermofisher.cdcam.enums.cdc.FederationProviders;
import com.thermofisher.cdcam.model.*;
import com.thermofisher.cdcam.model.cdc.*;
import com.thermofisher.cdcam.services.hashing.HashingService;
import com.thermofisher.cdcam.utils.AccountInfoHandler;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.CDCAccountsHandler;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class AccountRequestService {
    private Logger logger = LogManager.getLogger(this.getClass());

    @Value("${federation.aws.secret}")
    private String federationSecret;

    @Value("${aws.quick.sight.role}")
    String awsQuickSightRoleSecret;

    @Value("${aws.sns.reg.topic}")
    private String snsRegistrationTopic;

    @Value("${aws.sns.accnt.info.topic}")
    private String snsAccountInfoTopic;

    @Value("${tfrn.email-notification.url}")
    private String emailNotificationUrl;

    @Value("${tf.home}")
    private String redirectUrl;


    @Autowired
    SecretsManager secretsManager;

    @Autowired
    SNSHandler snsHandler;

    @Autowired
    AccountInfoHandler accountHandler;

    @Autowired
    HashValidationService hashValidationService;

    @Autowired
    CDCResponseHandler cdcResponseHandler;

    @Autowired
    HttpService httpService;

    @Autowired
    CDCAccountsService cdcAccountsService;


    @Async
    public void processRequest(String headerValue, String rawBody) {
        final int FED_PASSWORD_LENGTH = 10;

        logger.info("Async process for notify registration initiated.");
        try {
            JSONObject secretProperties = new JSONObject(secretsManager.getSecret(federationSecret));
            String key = secretsManager.getProperty(secretProperties, "cdc-secret-key");
            String hash = hashValidationService.getHashedString(key, rawBody);

            if (!hashValidationService.isValidHash(hash, headerValue)) {
                logger.error("Invalid hash signature.");
                return;
            }

            JSONObject mainObject = new JSONObject(rawBody);
            JSONArray events = (JSONArray) mainObject.get("events");

            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);
                JSONObject data = (JSONObject) event.get("data");

                if (!event.get("type").equals(Events.REGISTRATION.getValue())) {
                    logger.warn(String.format("Notify registration webhook event type was not recognized: %s", event.get("type")));
                    return;
                }

                String uid = data.get("uid").toString();

                setAwsQuickSightRole(uid);

                logger.info(String.format("Account UID: %s", uid));

                AccountInfo account = cdcResponseHandler.getAccountInfo(uid);
                if (account == null) {
                    logger.error(String.format("Account not found in CDC. UID: %s", uid));
                    return;
                }

                logger.info(String.format("Account username: %s. UID: %s", account.getUsername(), account.getUid()));
                String accountToNotify = accountHandler.prepareForProfileInfoNotification(account);
                try {
                    Map<String, MessageAttributeValue> messageAttributes = accountHandler.buildMessageAttributesForAccountInfoSNS(account);
                    boolean SNSSentCorrectly = snsHandler.sendSNSNotification(accountToNotify, snsAccountInfoTopic, messageAttributes);
                    if (!SNSSentCorrectly) {
                        logger.error(String.format("There was an error sending the account information to SNS Topic (%s). UID: %s", snsAccountInfoTopic, uid));
                    }
                    logger.info(String.format("Account Info Notification sent successfully. UID: %s", uid));
                } catch (Exception e) {
                    logger.error(String.format("Posting SNS Topic (%s) failed for UID: %s. Error: %s", snsAccountInfoTopic, uid, Utils.stackTraceToString(e)));
                }

                if (!hasFederationProvider(account)) {
                    logger.info(String.format("Account is not federated. UID: %s", account.getUid()));
                    return;
                }

                String duplicatedAccountUid = cdcResponseHandler.searchDuplicatedAccountUid(account.getUid(), account.getEmailAddress());
                boolean disableAccountStatus = cdcResponseHandler.disableAccount(duplicatedAccountUid);

                if (disableAccountStatus){
                    account.setDuplicatedAccountUid(duplicatedAccountUid);
                }

                if (account.getPassword().isEmpty()) {
                    account.setPassword(Utils.getAlphaNumericString(FED_PASSWORD_LENGTH));
                }
                String accountForGRP = accountHandler.prepareForGRPNotification(account);

                boolean SNSSentCorrectly = snsHandler.sendSNSNotification(accountForGRP, snsRegistrationTopic);
                if (!SNSSentCorrectly) {
                    logger.error(String.format("Posting SNS Topic (%s) failed for UID: %s.", snsRegistrationTopic, uid));
                    return;
                }
                logger.info(String.format("Account Registration Notification sent successfully. UID: %s", uid));
                return;
            }
            logger.error("No webhook events found in request.");

        } catch (Exception e) {
            logger.error(String.format("An error occurred while processing an account notify registration request. Error: %s", Utils.stackTraceToString(e)));
        }
    }

    public CDCResponseData processRegistrationRequest(AccountInfo accountInfo) {
        try {
            CDCNewAccount newAccount = CDCAccountsHandler.buildCDCNewAccount(accountInfo);            
            CDCResponseData cdcResponseData = cdcResponseHandler.register(newAccount);

            if (cdcResponseData != null) {
                if (cdcResponseData.getValidationErrors() != null ? cdcResponseData.getValidationErrors().size() == 0 : HttpStatus.valueOf(cdcResponseData.getStatusCode()).is2xxSuccessful()) {
                    accountInfo.setUid(cdcResponseData.getUID());
                    accountInfo.setPassword(HashingService.concat(HashingService.hash(accountInfo.getPassword())));

                    logger.info(String.format("Account registration successful. Username: %s. UID: %s.", accountInfo.getUsername(), accountInfo.getUid()));

                    String accountForGRP = accountHandler.prepareForGRPNotification(accountInfo);
                    boolean SNSSentCorrectly = snsHandler.sendSNSNotification(accountForGRP, snsRegistrationTopic);
                    if (!SNSSentCorrectly) {
                        logger.error(String.format("Posting SNS Topic (%s) failed for UID: %s.", snsRegistrationTopic, accountInfo.getUid()));
                    } else {
                        logger.info(String.format("Account Registration Notification sent successfully. UID: %s", accountInfo.getUid()));
                    }
                } else {
                    logger.error(String.format("An error occurred while processing an account registration request. Username: %s. Error: %s",
                            accountInfo.getUsername(), cdcResponseData.getStatusReason()));
                }
            }

            return cdcResponseData;

        } catch (Exception e) {
            logger.error(String.format("An error occurred while processing an account registration request. Error: %s", Utils.stackTraceToString(e)));
            return null;
        }
    }

    @Async
    public void sendConfirmationEmail(AccountInfo accountInfo) throws IOException {
        RegistrationConfirmation request = new RegistrationConfirmation().build(accountInfo, redirectUrl);
        JSONObject requestBody = new JSONObject(request);

        CloseableHttpResponse response = (httpService.post(emailNotificationUrl, requestBody)).getCloseableHttpResponse();

        HttpEntity responseEntity = response.getEntity();

        if (responseEntity != null) {
            int status = response.getStatusLine().getStatusCode();
            HttpStatus httpStatus = HttpStatus.valueOf(status);

            if (httpStatus.is2xxSuccessful()) {
                logger.info(String.format("Confirmation email sent to: %s", accountInfo.getEmailAddress()));
            } else {
                logger.warn(String.format("Something went wrong while sending the confirmation email to: %s. Status: %d",
                        accountInfo.getEmailAddress(), status));
            }

        } else {
            logger.error(String.format("Something went wrong while connecting to the email notification service. UID: %s", accountInfo.getUid()));
            throw new IOException();
        }
    }

    @Async
    public void sendVerificationEmail(String uid) {
        triggerVerificationEmailProcess(uid);
    }

    @Async
    public void setAwsQuickSightRole(String uid){
        String EMPTY_PROFILE = "";
        String QUICK_SIGHT_ROLE_PROPERTY = "awsQuickSightRole";
        logger.info("Async process for update aws quick sight role.");
        try {
            JSONObject secretProperties = new JSONObject(secretsManager.getSecret(awsQuickSightRoleSecret));
            String awsQuickSightRole = secretsManager.getProperty(secretProperties, QUICK_SIGHT_ROLE_PROPERTY);
            Data data = Data.builder().awsQuickSightRole(awsQuickSightRole).build();
            JSONObject jsonData = Utils.removeNullValuesFromJsonObject(new JSONObject(data));
            GSResponse response = cdcAccountsService.setUserInfo(uid,jsonData.toString(),EMPTY_PROFILE);
            if (response.getErrorCode() == 0) {
                logger.info("update aws quick sight role finished.");
            } else {
                logger.error(String.format("An error occurred while updating aws quick sight role finished. UID: %s. Error: %s", uid, response.getErrorDetails()));
            }
        }
        catch (Exception ex)
        {
            logger.error(Utils.stackTraceToString(ex));
        }
    }

    public CDCResponseData sendVerificationEmailSync(String uid) {
        return triggerVerificationEmailProcess(uid);
    }

    private CDCResponseData triggerVerificationEmailProcess(String uid) {
        CDCResponseData response = new CDCResponseData();

        try {
            response = cdcResponseHandler.sendVerificationEmail(uid);
            HttpStatus status = HttpStatus.valueOf(response.getStatusCode());

            if (status.is2xxSuccessful()) {
                logger.info(String.format("Verification email sent successfully. UID: %s", uid));
            } else {
                logger.info(String.format("Something went wrong while sending the verification email. UID: %s. Status: %d. Error: %s", uid, status.value(), response.getErrorDetails()));
            }
        } catch (Exception e) {
            logger.error(String.format("An exception occurred while sending the verification email to the user. UID: %s. Exception: %s", uid, Utils.stackTraceToString(e)));
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }

    private boolean hasFederationProvider(AccountInfo account) {
        return account.getLoginProvider().toLowerCase().contains(FederationProviders.OIDC.getValue()) || account.getLoginProvider().toLowerCase().contains(FederationProviders.SAML.getValue());
    }
}
