package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.enums.cdc.Events;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.CDCResponseData;
import com.thermofisher.cdcam.model.Profile;
import com.thermofisher.cdcam.utils.AccountInfoHandler;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AccountRequestService {
    static final Logger logger = LogManager.getLogger("CdcamApp");

    @Value("${federation.aws.secret}")
    private String federationSecret;

    @Value("${cdcam.reg.notification.url}")
    private String regNotificationUrl;

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
    NotificationService notificationService;

    @Autowired
    UpdateAccountService updateAccountService;

    @Async
    public void processRequest(String headerValue, String rawBody) {
        final int FED_PASSWORD_LENGTH = 10;

        try {
            JSONObject secretProperties = new JSONObject(secretsManager.getSecret(federationSecret));
            String key = secretsManager.getProperty(secretProperties, "cdc-secret-key");
            String hash = hashValidationService.getHashedString(key, rawBody);

            if (!hashValidationService.isValidHash(hash, headerValue)) {
                logger.error("INVALID SIGNATURE");
                return;
            }

            JSONObject mainObject =  new JSONObject(rawBody) ;
            JSONArray events = (JSONArray) mainObject.get("events");

            for (int i =0;i<events.length() ; i++) {
                JSONObject event = events.getJSONObject(i);
                JSONObject data = (JSONObject) event.get("data");

                if (!event.get("type").equals(Events.REGISTRATION.getValue())) {
                    logger.error("The event type was not recognized");
                    return;
                }

                String uid = data.get("uid").toString();
                AccountInfo account = cdcResponseHandler.getAccountInfo(uid);
                if (account == null) {
                    logger.error("Account not found. UID: " + uid);
                    return;
                }

                String accountToNotify = accountHandler.prepareForProfileInfoNotification(account);
                try {
                    CloseableHttpResponse response = notificationService.postRequest(accountToNotify, regNotificationUrl);
                    logger.info("Response:  " + response.getStatusLine().getStatusCode() + ". Response message: " + EntityUtils.toString(response.getEntity()));
                    response.close();
                } catch (Exception e) {
                    logger.error("EXCEPTION: The call to " + regNotificationUrl + " has failed with errors " + e.getMessage());
                }

                updateAccountService.updateLegacyDataInCDC(uid, account.getEmailAddress());
                if (account.getPassword().isEmpty()) {
                    account.setPassword(Utils.getAlphaNumericString(FED_PASSWORD_LENGTH));
                }
                String accountForGRP = accountHandler.prepareForGRPNotification(account);

                boolean SNSSentCorrectly = snsHandler.sendSNSNotification(accountForGRP);
                if (!SNSSentCorrectly) {
                    logger.error("The user was not created through federation.");
                    return;
                }
                logger.info("User sent to SNS.");
                return;
            }
            logger.error("NO EVENT FOUND");

        } catch (Exception e) {
            Utils.logStackTrace(e,logger);
        }
    }

    public CDCResponseData processRegistrationRequest(AccountInfo accountInfo) {
        try {

            Profile profile = Profile.builder()
                    .firstName(accountInfo.getFirstName())
                    .lastName(accountInfo.getLastName()).build();
           String json = accountHandler.prepareProfileForRegistration(profile);

            return cdcResponseHandler.register(accountInfo.getUsername(), accountInfo.getEmailAddress(), accountInfo.getPassword(), "", json);

        } catch (Exception e) {
            Utils.logStackTrace(e,logger);
            return null;
        }
    }
}
