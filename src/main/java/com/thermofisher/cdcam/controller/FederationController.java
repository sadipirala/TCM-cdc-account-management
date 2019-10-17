package com.thermofisher.cdcam.controller;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.enums.cdc.Events;
import com.thermofisher.cdcam.enums.cdc.FederationProviders;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.services.CDCAccountsService;
import com.thermofisher.cdcam.services.HashValidationService;
import com.thermofisher.cdcam.services.NotificationService;
import com.thermofisher.cdcam.utils.AccountInfoHandler;

import com.thermofisher.cdcam.utils.Utils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/federation")
public class FederationController {
    static final Logger logger = LogManager.getLogger("CdcamApp");

    @Value("${federation.aws.secret}")
    private String federationSecret;

    @Value("${cdcam.reg.notification.url}")
    private String regNotificationUrl;

    @Autowired
    AccountInfoHandler accountHandler;

    @Autowired
    SecretsManager secretsManager;

    @Autowired
    SNSHandler snsHandler;

    @Autowired
    CDCAccountsService accountsService;

    @Autowired
    HashValidationService hashValidationService;

    @Autowired
    NotificationService notificationService;

    @PostMapping("/user")
    public ResponseEntity<String> notifyRegistration(@RequestHeader("x-gigya-sig-hmac-sha1") String headerValue, @RequestBody String rawBody) {
        try {
            JSONObject secretProperties = (JSONObject) new JSONParser().parse(secretsManager.getSecret(federationSecret));
            String key = secretsManager.getProperty(secretProperties, "cdc-secret-key");

            if (!hashValidationService.isValidHash(hashValidationService.getHashedString(key, rawBody), headerValue)) {
                logger.error("INVALID SIGNATURE");
                return new ResponseEntity<>("INVALID SIGNATURE", HttpStatus.BAD_REQUEST);
            }

            JSONParser parser = new JSONParser();
            JSONObject mainObject = (JSONObject) parser.parse(rawBody);
            JSONArray events = (JSONArray) mainObject.get("events");

            for (Object singleEvent : events) {
                JSONObject event = (JSONObject) singleEvent;
                JSONObject data = (JSONObject) event.get("data");

                if (!event.get("type").equals(Events.REGISTRATION.getValue())) {
                    logger.error("The event type was not recognized");
                    return new ResponseEntity<>("the event type was not recognized", HttpStatus.OK);
                }

                String uid = data.get("uid").toString();
                AccountInfo account = accountsService.getFederationAccountInfo(uid);
                if (account == null){
                    logger.fatal("Account is null");
                }
                String accountToNotify = accountHandler.parseToNotify(account);
                logger.error("Call parse to notify, parse account: " + accountToNotify );
                try{
                    CloseableHttpResponse notificationPostResponse = notificationService.postRequest(accountToNotify,regNotificationUrl);
                    logger.fatal("The call to " + regNotificationUrl + " has finished with response code " + notificationPostResponse.getStatusLine().getStatusCode());
                }
                catch (Exception e){
                    logger.fatal("The call to " + regNotificationUrl + " has failed with errors " + e.getMessage());
                }

                if (account == null) {
                    logger.error("The user was not created through federation.");
                    return new ResponseEntity<>("NO USER FOUND", HttpStatus.BAD_REQUEST);
                }

                if (!hasFederationProvider(account)) {
                    logger.error("The user was not created through federation.");
                    return new ResponseEntity<>("The user was not created through federation.", HttpStatus.OK);
                }

                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writeValueAsString(account);
                System.out.println("JSON: " + jsonString);
                if (!snsHandler.sendSNSNotification(jsonString)) {
                    logger.error("The user was not created through federation.");
                    return new ResponseEntity<>("Something went wrong... An SNS Notification failed to be sent.", HttpStatus.SERVICE_UNAVAILABLE);
                }

                logger.info("User sent to SNS.");
                return new ResponseEntity<>(jsonString, HttpStatus.OK);
            }

            logger.error("NO EVENT FOUND");
            return new ResponseEntity<>("NO EVENT FOUND", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            logger.fatal(stackTrace);
            return new ResponseEntity<>("ERROR: " + stackTrace, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean hasFederationProvider(AccountInfo account) {
        return account.getLoginProvider().toLowerCase().contains(FederationProviders.OIDC.getValue()) || account.getLoginProvider().toLowerCase().contains(FederationProviders.SAML.getValue());
    }
}
