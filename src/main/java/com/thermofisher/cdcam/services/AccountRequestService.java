package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.enums.cdc.Events;
import com.thermofisher.cdcam.enums.cdc.FederationProviders;
import com.thermofisher.cdcam.model.*;
import com.thermofisher.cdcam.services.hashing.HashingService;
import com.thermofisher.cdcam.utils.AccountInfoHandler;
import com.thermofisher.cdcam.utils.Utils;
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

@Service
public class AccountRequestService {
    private Logger logger = LogManager.getLogger(this.getClass());

    @Value("${federation.aws.secret}")
    private String federationSecret;

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
    UpdateAccountService updateAccountService;

    @Autowired
    HttpService httpService;

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
                logger.info(String.format("Account UID: %s", uid));

                AccountInfo account = cdcResponseHandler.getAccountInfo(uid);
                if (account == null) {
                    logger.error(String.format("Account not found in CDC. UID: %s", uid));
                    return;
                }

                logger.info(String.format("Account username: %s. UID: %s", account.getUsername(), account.getUid()));
                String accountToNotify = accountHandler.prepareForProfileInfoNotification(account);
                try {
                    boolean SNSSentCorrectly = snsHandler.sendSNSNotification(accountToNotify, snsAccountInfoTopic);
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

                updateAccountService.updateLegacyDataInCDC(uid, account.getEmailAddress());
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
            Data data = Data.builder()
                    .subscribe(accountInfo.getMember())
                    .build();
            Work work = Work.builder()
                    .company(accountInfo.getCompany())
                    .location(accountInfo.getDepartment())
                    .build();
            Profile profile = Profile.builder()
                    .firstName(accountInfo.getFirstName())
                    .lastName(accountInfo.getLastName())
                    .country(accountInfo.getCountry())
                    .city(accountInfo.getCity())
                    .work(work)
                    .build();

            String jsonProfile = accountHandler.prepareProfileForRegistration(profile);
            String jsonData = accountHandler.prepareDataForRegistration(data);
            CDCResponseData cdcResponseData = cdcResponseHandler.register(accountInfo.getUsername(), accountInfo.getEmailAddress(), accountInfo.getPassword(), jsonData, jsonProfile);

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

    private boolean hasFederationProvider(AccountInfo account) {
        return account.getLoginProvider().toLowerCase().contains(FederationProviders.OIDC.getValue()) || account.getLoginProvider().toLowerCase().contains(FederationProviders.SAML.getValue());
    }

    @Async
    public void sendConfirmationEmail(AccountInfo account) throws IOException {
        RegistrationConfirmation regConfirmation = new RegistrationConfirmation().build(account, redirectUrl);
        JSONObject body = new JSONObject(regConfirmation);

        CloseableHttpResponse response = httpService.post(emailNotificationUrl, body);

        HttpEntity responseEntity = response.getEntity();

        if(responseEntity != null) {
            int statusCode = response.getStatusLine().getStatusCode();
            HttpStatus httpStatus = HttpStatus.valueOf(statusCode);

            if (httpStatus.is2xxSuccessful()) {
                logger.info(String.format("Confirmation email sent to: %s", account.getEmailAddress()));
            } else {
                logger.warn(String.format("Something went wrong while sending the confirmation email to: %s. Status: %d",
                        account.getEmailAddress(), statusCode));
            }
        } else {
            throw new IOException();
        }
    }
}
