package com.thermofisher.cdcam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.enums.cdc.FederationProviders;
import com.thermofisher.cdcam.model.AccountInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountController {
    static final Logger logger = LogManager.getLogger("CdcamApp");

    @Autowired
    SNSHandler snsHandler;
    @Autowired
    CDCAccounts accounts;

    @PostMapping("/user")
    public ResponseEntity<String> notifyRegistration() {
        try {

            AccountInfo account = accounts.getAccount("ffb10070d8174a518f2e8b403c1efe5d");
            if ((account.getLoginProvider()).toLowerCase().contains(FederationProviders.OIDC.getValue()) || (account.getLoginProvider()).toLowerCase().contains(FederationProviders.SAML.getValue())) {
                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writeValueAsString(account);
                if (snsHandler.sendSNSNotification(jsonString)) {
                    return new ResponseEntity<>(jsonString, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("Something went wrong... An SNS Notification failed to be sent.",
                            HttpStatus.SERVICE_UNAVAILABLE);
                }
            } else {
                return new ResponseEntity<>("NON FEDERATION USER", HttpStatus.OK);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>("ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
