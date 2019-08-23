package com.thermofisher.cdcam.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.environment.ApplicationConfiguration;
import com.thermofisher.cdcam.model.AccountInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.AppendersPlugin;
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
    ApplicationConfiguration applicationConfiguration;
    @Autowired
    SNSHandler snsHandler;

    @PostMapping("/user")
    public ResponseEntity<String> registerUser() {
        try {
            CDCAccounts accounts = new CDCAccounts(applicationConfiguration);
            AccountInfo account = accounts.getAccount("ffb10070d8174a518f2e8b403c1efe5d");
            if((account.getLoginProvider()).toLowerCase().contains("oidc") || (account.getLoginProvider()).toLowerCase().contains("saml") ) {
                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writeValueAsString(account);
                snsHandler.sendSNSNotification(applicationConfiguration.getAWSSNSTopic(), jsonString);
                return new ResponseEntity<>(jsonString, HttpStatus.OK);
            }
            else
            {
                return new ResponseEntity<>("error", HttpStatus.BAD_REQUEST);
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
