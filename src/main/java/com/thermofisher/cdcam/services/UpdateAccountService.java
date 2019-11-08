package com.thermofisher.cdcam.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.thermofisher.cdcam.model.CDCAccount;
import com.thermofisher.cdcam.model.Data;
import com.thermofisher.cdcam.model.Profile;
import com.thermofisher.cdcam.model.Thermofisher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class UpdateAccountService {
    static final Logger logger = LogManager.getLogger("CdcamApp");
    private static final int SUCCESS_CODE = 200;

    @Autowired
    CDCAccountsService cdcAccountsService;

    @Async
    public void updateLegacyDataInCDC(String uid, String emailAddress) throws JSONException {
        Thermofisher thermofisher = Thermofisher.builder().legacyEmail(emailAddress).legacyUsername(emailAddress).build();
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
}
