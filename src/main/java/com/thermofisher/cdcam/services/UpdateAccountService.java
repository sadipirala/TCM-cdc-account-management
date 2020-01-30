package com.thermofisher.cdcam.services;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.cdcam.model.CDCAccount;
import com.thermofisher.cdcam.model.Data;
import com.thermofisher.cdcam.model.Profile;
import com.thermofisher.cdcam.model.Thermofisher;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

@Service
public class UpdateAccountService {
    static final Logger logger = LogManager.getLogger("CdcamApp");
    private static final int SUCCESS_CODE = 200;

    @Autowired
    CDCResponseHandler cdcAccountsService;

    @Async
    public void updateLegacyDataInCDC(String uid, String emailAddress) throws JSONException {
        Thermofisher thermofisher = Thermofisher.builder().legacyEmail(emailAddress).legacyUsername(emailAddress)
                .build();
        Data data = Data.builder().thermofisher(thermofisher).build();
        Profile profile = Profile.builder().username(emailAddress).build();
        CDCAccount account = new CDCAccount();
        account.setUID(uid);
        account.setProfile(profile);
        account.setData(data);

        JSONObject jsonAccount = new JSONObject();
        jsonAccount.put("uid", uid);
        jsonAccount.put("data", new JSONObject(data));
        jsonAccount.put("profile", new JSONObject(profile));
        JsonNode response = cdcAccountsService.update(jsonAccount);
        if (response.get("code").asInt() == SUCCESS_CODE) {
            logger.info("uid: " + uid + " updated.");
        } else {
            logger.fatal("uid: " + uid + " failed. error Code: " + response.get("log").asText());
        }
    }

    public HttpStatus updateTimezoneInCDC(String uid, String timezone) throws JSONException, JsonProcessingException {
        Profile profile = Profile.builder().timezone(timezone).build();
        ObjectMapper mapper = new ObjectMapper();
        JSONObject jsonAccount = new JSONObject();

        List<String> propertiesToRemove = new ArrayList<>();
        propertiesToRemove.add("username");
        propertiesToRemove.add("email");
        propertiesToRemove.add("firstName");
        propertiesToRemove.add("lastName");
        ObjectNode jsonProfile = mapper.valueToTree(profile);
        jsonProfile.remove(propertiesToRemove);
        jsonAccount.put("uid", uid);
        jsonAccount.put("profile", new JSONObject(mapper.writeValueAsString(jsonProfile)));
        ObjectNode response = cdcAccountsService.update(jsonAccount);

        if (response.get("code").asInt() == SUCCESS_CODE) {
            logger.info("uid: " + uid + " updated.");
        } else {
            logger.fatal("uid: " + uid + " failed. error Code: " + response.get("log").asText());
        }

        return HttpStatus.valueOf(response.get("code").asInt());
    }
}
