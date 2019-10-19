package com.thermofisher.cdcam.services;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.utils.Utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * CDCAccountsService
 */
@Configuration
public class CDCAccountsService {
    private final int SUCCESS_CODE = 0;
    static final Logger logger = LogManager.getLogger("CdcamApp");
    private final AccountBuilder accountBuilder = new AccountBuilder();

    @Autowired
    CDCAccounts cdcAccounts;

    public AccountInfo getAccountInfo(String uid) {
        GSResponse response = cdcAccounts.getAccount(uid);
        if (response.getErrorCode() == 0) {
            GSObject obj = response.getData();
            return accountBuilder.getAccountInfo(obj);
        } else {
            logger.fatal("GET_ACCOUNT: " + response.getErrorDetails());
            return null;
        }
    }

    public ObjectNode update(JSONObject user) {
        if (user == null) return null;

        String uid = Utils.getStringFromJSON(user, "uid");
        JSONObject data = Utils.getObjectFromJSON(user,"data");
        JSONObject profile = Utils.getObjectFromJSON(user, "profile");

        String dataJson = (data != null) ? data.toString() : "";
        String profileJson = (profile != null) ? profile.toString() : "";

        GSResponse response = cdcAccounts.setUserInfo(uid, dataJson, profileJson);

        ObjectNode json = JsonNodeFactory.instance.objectNode();
        if (response.getErrorCode() == SUCCESS_CODE) {
            json.put("code", HttpStatus.OK.value());
        } else {
            json.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        json.put("message", response.getErrorMessage());
        
        return json;
    }
}
