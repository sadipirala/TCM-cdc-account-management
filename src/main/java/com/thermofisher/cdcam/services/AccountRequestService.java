package com.thermofisher.cdcam.services;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotBlank;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.enums.aws.CdcamSecrets;
import com.thermofisher.cdcam.enums.cdc.FederationProviders;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.RegistrationConfirmation;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.Data;
import com.thermofisher.cdcam.model.notifications.AccountUpdatedNotification;
import com.thermofisher.cdcam.model.notifications.MergedAccountNotification;
import com.thermofisher.cdcam.services.hashing.HashingService;
import com.thermofisher.cdcam.utils.AccountInfoHandler;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.CDCAccountsHandler;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AccountRequestService {
    private Logger logger = LogManager.getLogger(this.getClass());
    private final int FED_PASSWORD_LENGTH = 10;

    @Value("${aws.sns.reg.topic}")
    private String snsRegistrationTopic;

    @Value("${aws.sns.accnt.info.topic}")
    private String snsAccountInfoTopic;

    @Value("${tfrn.email-notification.url}")
    private String emailNotificationUrl;

    @Value("${tf.home}")
    private String redirectUrl;

    @Autowired
    AccountInfoHandler accountHandler;

    @Autowired
    CDCAccountsService cdcAccountsService;

    @Autowired
    CDCResponseHandler cdcResponseHandler;

    @Autowired
    HttpService httpService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    SecretsService secretsService;

    @Autowired
    SNSHandler snsHandler;

    @Async
    public void onAccountRegistered(@NotBlank String uid) {
        Objects.requireNonNull(uid);
        logger.info(String.format("Async process for onAccountRegistered initiated for UID: %s", uid));

        try {
            setAwsQuickSightRole(uid);

            AccountInfo account = cdcResponseHandler.getAccountInfo(uid);
            logger.info(String.format("Account username: %s. UID: %s", account.getUsername(), account.getUid()));

            String accountToNotify = accountHandler.prepareForProfileInfoNotification(account);
            Map<String, MessageAttributeValue> messageAttributes = accountHandler.buildMessageAttributesForAccountInfoSNS(account);

            try {
                snsHandler.sendNotification(accountToNotify, snsAccountInfoTopic, messageAttributes);
                logger.info(String.format("Account Info Notification sent successfully. UID: %s", uid));
            } catch (Exception e) {
                logger.error(String.format("Account Info notification error: %s", e.getMessage()));
            }

            if (hasFederationProvider(account)) {
                if (account.getPassword().isEmpty()) {
                    account.setPassword(Utils.getAlphaNumericString(FED_PASSWORD_LENGTH));
                }
                
                String accountForGRP = accountHandler.buildRegistrationNotificationPayload(account);
                snsHandler.sendNotification(accountForGRP, snsRegistrationTopic);
                logger.info(String.format("Account Registration Notification sent successfully. UID: %s", uid));
            }
        } catch (Exception e) {
            logger.error(Utils.stackTraceToString(e));
            logger.error(String.format("Error: %s. UID: %s.", e.getMessage(), uid));
        }
    }

    public CDCResponseData processRegistrationRequest(AccountInfo accountInfo) {
        try {
            CDCNewAccount newAccount = CDCAccountsHandler.buildCDCNewAccount(accountInfo);
            CDCResponseData cdcResponseData = cdcResponseHandler.register(newAccount);

            if (cdcResponseData != null) {
                if (cdcResponseData.getValidationErrors() != null ? cdcResponseData.getValidationErrors().size() == 0 : HttpStatus.valueOf(cdcResponseData.getStatusCode()).is2xxSuccessful()) {
                    accountInfo.setUid(cdcResponseData.getUID());
                    String hashedPassword = HashingService.toMD5(accountInfo.getPassword());
                    accountInfo.setPassword(hashedPassword);

                    logger.info(String.format("Account registration successful. Username: %s. UID: %s.", accountInfo.getUsername(), accountInfo.getUid()));

                    String accountForGRP = accountHandler.buildRegistrationNotificationPayload(accountInfo);
                    snsHandler.sendNotification(accountForGRP, snsRegistrationTopic);
                    logger.info(String.format("Account Registration Notification sent successfully. UID: %s", accountInfo.getUid()));
                } else {
                    String error = String.format("Error on account registration request. Username: %s. Error: %s", accountInfo.getUsername(), cdcResponseData.getStatusReason());
                    logger.error(error);
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
    public void setAwsQuickSightRole(String uid) {
        String EMPTY_PROFILE = "";

        logger.info("Async process for update aws quick sight role.");
        try {
            String awsQuickSightRole = secretsService.get(CdcamSecrets.QUICKSIGHT_ROLE.getKey());
            Data data = Data.builder().awsQuickSightRole(awsQuickSightRole).build();
            JSONObject jsonData = Utils.removeNullValuesFromJsonObject(new JSONObject(data));
            
            GSResponse response = cdcAccountsService.setUserInfo(uid, jsonData.toString(), EMPTY_PROFILE);
            
            if (response.getErrorCode() == 0) {
                logger.info("update aws quick sight role finished.");
            } else {
                logger.error(String.format("An error occurred while updating aws quick sight role finished. UID: %s. Error: %s", uid, response.getErrorDetails()));
            }
        }
        catch (Exception ex) {
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

    @Async
    public void onAccountMerged(@NotBlank String uid) {
        Objects.requireNonNull(uid);

        logger.info(String.format("Account linking merge process started for UID: %s", uid));
        try {
            AccountInfo accountInfo = cdcResponseHandler.getAccountInfo(uid);
            if (!accountInfo.isFederatedAccount()) {
                logger.info(String.format("Merge update process stopped. Account %s with UID %s is not federated. Merge update is only supported for federated accounts.", accountInfo.getUsername(), uid));
                return;
            }

            logger.info("Setting random password for merged account notification.");
            accountInfo.setPassword(Utils.getAlphaNumericString(FED_PASSWORD_LENGTH));
            logger.info("Building MergedAccountNotification object.");
            MergedAccountNotification mergedAccountNotification = MergedAccountNotification.build(accountInfo);
            logger.info("Sending accountMerged notification.");
            notificationService.sendAccountMergedNotification(mergedAccountNotification);
            logger.info("accountMerged notification sent.");
        } catch (Exception e) {
            logger.error(String.format("onAccountMerged - Something went wrong. %s.", e.getMessage()));
        }
    }

    @Async
    public void onAccountUpdated(@NotBlank String uid) {
        logger.info(String.format("Account linking update process started for UID: %s", uid));
        try {
            AccountInfo accountInfo = cdcResponseHandler.getAccountInfo(uid);
            if (!accountInfo.isFederatedAccount()) {
                logger.info(String.format("Update process stopped. Account %s with UID %s is not federated. Update is only supported for federated accounts.", accountInfo.getUsername(), uid));
                return;
            }

            logger.info("Building AccountUpdatedNotification object.");
            AccountUpdatedNotification accountUpdatedNotification = AccountUpdatedNotification.build(accountInfo);
            logger.info("Sending accountUpdated notification.");
            notificationService.sendPrivateAccountUpdatedNotification(accountUpdatedNotification);
            logger.info("accountUpdated notification sent.");
        } catch (Exception e) {
            logger.error(String.format("onAccountUpdated - Something went wrong. %s.", e.getMessage()));
        }
    }
}
