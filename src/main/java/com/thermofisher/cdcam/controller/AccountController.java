package com.thermofisher.cdcam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.enums.cdc.Events;
import com.thermofisher.cdcam.enums.cdc.FederationProviders;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.services.HashValidationService;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/account")
public class AccountController {
    static final Logger logger = LogManager.getLogger("CdcamApp");

    @Autowired
    SNSHandler snsHandler;
    @Autowired
    CDCAccounts accounts;
    @Autowired
    HashValidationService hashValidationService;

    @PostMapping("/user")
    public ResponseEntity<String> notifyRegistration(HttpServletRequest request) {
        try {
            String headerValue = request.getHeader("X-Gigya-Sig-Hmac-Sha1");
            String rawBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
            if (hashValidationService.isValidHash(hashValidationService.getHashedString(rawBody), headerValue)) {
                JSONParser parser = new JSONParser();
                JSONObject mainObject = (JSONObject) parser.parse(rawBody);
                JSONArray events = (JSONArray)mainObject.get("events");
                for (Object o : events) {
                    JSONObject event = (JSONObject) o;
                    JSONObject data = (JSONObject) event.get("data");
                    if (event.get("type").equals(Events.REGISTRATION.getValue())) {
                        String uid = data.get("uid").toString();
                        AccountInfo account = accounts.getAccount(uid);
                        if ((account.getLoginProvider()).toLowerCase().contains(FederationProviders.OIDC.getValue()) || (account.getLoginProvider()).toLowerCase().contains(FederationProviders.SAML.getValue())) {
                            ObjectMapper mapper = new ObjectMapper();
                            String jsonString = mapper.writeValueAsString(account);
                            if (snsHandler.sendSNSNotification(jsonString)) {
                                logger.info("User sent");
                                return new ResponseEntity<>(jsonString, HttpStatus.OK);
                            } else {
                                logger.error("The user was not created through federation");
                                return new ResponseEntity<>("Something went wrong... An SNS Notification failed to be sent.", HttpStatus.SERVICE_UNAVAILABLE);
                            }
                        } else {
                            logger.error("The user was not created through federation");
                            return new ResponseEntity<>("The user was not created through federation", HttpStatus.OK);
                        }
                    }
                    else {
                        logger.error("The event type was not recognized");
                        return new ResponseEntity<>("the event type was not recognized", HttpStatus.OK);
                    }
                }
                logger.error("NO EVENT FOUND");
                return new ResponseEntity<>("NO EVENT FOUND", HttpStatus.BAD_REQUEST);
            }
            else{
                logger.error("INVALID SIGNATURE");
                return new ResponseEntity<>("INVALID SIGNATURE", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>("ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
