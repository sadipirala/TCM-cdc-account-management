package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.enums.cdc.Events;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.utils.AccountInfoHandler;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
import com.thermofisher.cdcam.utils.cdc.UsersHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;

@Service
public class AccountRequestService {
    static final Logger logger = LogManager.getLogger("CdcamApp");

    @Value("${federation.aws.secret}")
    private String federationSecret;

    @Value("${cdcam.reg.notification.url}")
    private String regNotificationUrl;

    @Autowired
    CDCAccounts cdcAccounts;

    @Autowired
    SecretsManager secretsManager;

    @Autowired
    SNSHandler snsHandler;

    @Autowired
    LiteRegHandler handler;

    @Autowired
    AccountInfoHandler accountHandler;

    @Autowired
    UsersHandler usersHandler;

    @Autowired
    CDCAccountsService cdcAccountsService;

    @Autowired
    HashValidationService hashValidationService;

    @Autowired
    CDCAccountsService accountsService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    UpdateAccountService updateAccountService;

    @Async
    public void processRequest( String headerValue, String rawBody){
        final int FED_PASSWORD_LENGTH = 10;

        try {
            JSONObject secretProperties =  (JSONObject) new JSONParser().parse(secretsManager.getSecret(federationSecret));
            String key = secretsManager.getProperty(secretProperties, "cdc-secret-key");
            String hash = hashValidationService.getHashedString(key, rawBody);

            if (!hashValidationService.isValidHash(hash, headerValue)) {
                logger.error("INVALID SIGNATURE");
                return;
            }

            JSONParser parser = new JSONParser();
            JSONObject mainObject = (JSONObject) parser.parse(rawBody);
            JSONArray events = (JSONArray) mainObject.get("events");

            for (Object singleEvent : events) {
                JSONObject event = (JSONObject) singleEvent;
                JSONObject data = (JSONObject) event.get("data");

                if (!event.get("type").equals(Events.REGISTRATION.getValue())) {
                    logger.error("The event type was not recognized");
                    return;
                }

                String uid = data.get("uid").toString();
                AccountInfo account = accountsService.getAccountInfo(uid);
                if (account == null) {
                    logger.error("Account not found. UID: " + uid);
                    return;
                }

                String accountToNotify = accountHandler.prepareForProfileInfoNotification(account);
                try {
                    CloseableHttpResponse response = notificationService.postRequest(accountToNotify, regNotificationUrl);
                    logger.info("Response:  " + response.getStatusLine().getStatusCode() + ". Response message: " + EntityUtils.toString(response.getEntity()));
                    response.close();
                }
                catch (Exception e) {
                    logger.error("EXCEPTION: The call to " + regNotificationUrl + " has failed with errors " + e.getMessage());
                }

                updateAccountService.updateLegacyDataInCDC(uid, account.getEmailAddress());
                account.setPassword(Utils.getAlphaNumericString(FED_PASSWORD_LENGTH));
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
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            logger.error(stackTrace);
        }
    }
}
