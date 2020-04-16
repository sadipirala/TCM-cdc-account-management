package com.thermofisher.cdcam.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.cdcam.model.Data;
import com.thermofisher.cdcam.model.Profile;
import com.thermofisher.cdcam.model.Thermofisher;
import com.thermofisher.cdcam.utils.Utils;
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
    private Logger logger = LogManager.getLogger(this.getClass());
    private static final int SUCCESS_CODE = 200;

    @Autowired
    CDCResponseHandler cdcAccountsService;

    @Async
    public void updateLegacyDataInCDC(String uid, String emailAddress) throws JSONException, JsonProcessingException {
        logger.info(String.format("Account update for legacy data triggered. UID: %s", uid));

        Thermofisher thermofisher = Thermofisher.builder().legacyEmail(emailAddress).legacyUsername(emailAddress)
                .build();
        Data data = Data.builder().thermofisher(thermofisher).build();
        Profile profile = Profile.builder().username(emailAddress).build();

        JSONObject jsonAccount = new JSONObject();

        jsonAccount.put("uid", uid);

        JSONObject dataJson = Utils.removeNullValuesFromJsonObject(new JSONObject(data));
        jsonAccount.put("data", dataJson);

        JSONObject profileJson = Utils.removeNullValuesFromJsonObject(new JSONObject(profile));
        jsonAccount.put("profile", profileJson);

        JsonNode response = cdcAccountsService.update(jsonAccount);
        if (response.get("code").asInt() == SUCCESS_CODE) {
            logger.info(String.format("Account update success. UID: %s", uid));
        } else {
            logger.error(String.format("Account update failed. UID: %s. Error: %s", uid, response.get("log").asText()));
        }
    }

    public HttpStatus updateTimezoneInCDC(String uid, String timezone) throws JSONException, JsonProcessingException {
        logger.info(String.format("Account update for time zone triggered. UID: %s", uid));

        Profile profile = Profile.builder().timezone(timezone).build();
        JSONObject jsonAccount = new JSONObject();

        JSONObject cleanProfile = Utils.removeNullValuesFromJsonObject(new JSONObject(profile));

        jsonAccount.put("uid", uid);
        jsonAccount.put("profile", cleanProfile);
        ObjectNode response = cdcAccountsService.update(jsonAccount);

        if (response.get("code").asInt() == SUCCESS_CODE) {
            logger.info(String.format("Account update success. UID: %s", uid));
        } else {
            logger.error(String.format("Account update failed. UID: %s. Error: %s", uid, response.get("log").asText()));
        }

        return HttpStatus.valueOf(response.get("code").asInt());
    }
}
