package com.thermofisher.cdcam.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.cdcam.model.Profile;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UpdateAccountService {
    private Logger logger = LogManager.getLogger(this.getClass());
    private static final int SUCCESS_CODE = 200;

    @Autowired
    CDCResponseHandler cdcAccountsService;

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
