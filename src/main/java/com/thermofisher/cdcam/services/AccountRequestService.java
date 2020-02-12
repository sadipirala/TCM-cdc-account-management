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

    @Value("${aws.sns.reg.topic}")
    private String snsRegistrationTopic;

    @Value("${aws.sns.accnt.info.topic}")
    private String snsAccountInfoTopic;

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

            JSONObject mainObject = new JSONObject(rawBody);
            JSONArray events = (JSONArray) mainObject.get("events");

            for (int i = 0; i < events.length(); i++) {
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
                    boolean SNSSentCorrectly = snsHandler.sendSNSNotification(accountToNotify, snsAccountInfoTopic);
                    if (!SNSSentCorrectly) {
                        logger.error("There was an error sending the account information.");
                    }
                    logger.info("Account info sent to SNS.");
                } catch (Exception e) {
                    logger.error("EXCEPTION: The call to SNS has failed with errors " + e.getMessage());
                }

                if (!hasFederationProvider(account)) {
                    logger.info("The user was not created through federation.");
                    return;
                }

                updateAccountService.updateLegacyDataInCDC(uid, account.getEmailAddress());
                if (account.getPassword().isEmpty()) {
                    account.setPassword(Utils.getAlphaNumericString(FED_PASSWORD_LENGTH));
                }
                String accountForGRP = accountHandler.prepareForGRPNotification(account);

                boolean SNSSentCorrectly = snsHandler.sendSNSNotification(accountForGRP, snsRegistrationTopic);
                if (!SNSSentCorrectly) {
                    logger.error("The user was not created through federation.");
                    return;
                }
                logger.info("User sent to SNS.");
                return;
            }
            logger.error("NO EVENT FOUND");

        } catch (Exception e) {
            Utils.logStackTrace(e, logger);
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
                if (cdcResponseData.getValidationErrors() != null ? cdcResponseData.getValidationErrors().size() == 0 : cdcResponseData.getStatusCode() == 200) {
                    accountInfo.setUid(cdcResponseData.getUID());
                    accountInfo.setPassword(HashingService.concat(HashingService.hash(accountInfo.getPassword())));
                    String accountForGRP = accountHandler.prepareForGRPNotification(accountInfo);
                    boolean SNSSentCorrectly = snsHandler.sendSNSNotification(accountForGRP, snsRegistrationTopic);
                    if (!SNSSentCorrectly) {
                        logger.error("The user was not created through federation.");
                    }
                    logger.info("User sent to SNS.");
                }
            }

            return cdcResponseData;

        } catch (Exception e) {
            Utils.logStackTrace(e, logger);
            return null;
        }
    }

    private boolean hasFederationProvider(AccountInfo account) {
        return account.getLoginProvider().toLowerCase().contains(FederationProviders.OIDC.getValue()) || account.getLoginProvider().toLowerCase().contains(FederationProviders.SAML.getValue());
    }
}
