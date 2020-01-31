package com.thermofisher.cdcam.utils.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.model.CDCResponseData;
import com.thermofisher.cdcam.services.CDCAccountsService;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.utils.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.io.IOException;

/**
 * CDCAccountsService
 */
@Configuration
public class CDCResponseHandler {
    private final int SUCCESS_CODE = 0;
    static final Logger logger = LogManager.getLogger("CdcamApp");
    private final AccountBuilder accountBuilder = new AccountBuilder();

    @Autowired
    CDCAccountsService cdcAccountsService;

    public AccountInfo getAccountInfo(String uid) {
        GSResponse response = cdcAccountsService.getAccount(uid);
        if (response.getErrorCode() == 0) {
            GSObject obj = response.getData();
            return accountBuilder.getAccountInfo(obj);
        } else {
            logger.error("Get account error: " + response.getErrorDetails());
            return null;
        }
    }

    public ObjectNode update(JSONObject account) {
        if (account == null) return null;
        String uid = Utils.getStringFromJSON(account, "uid");
        JSONObject data = Utils.getObjectFromJSON(account, "data");
        JSONObject profile = Utils.getObjectFromJSON(account, "profile");

        String dataJson = (data != null) ? data.toString() : "";
        String profileJson = (profile != null) ? profile.toString() : "";

        GSResponse cdcResponse = cdcAccountsService.setUserInfo(uid, dataJson, profileJson);

        ObjectNode response = JsonNodeFactory.instance.objectNode();
        if (cdcResponse.getErrorCode() == SUCCESS_CODE) {
            response.put("code", HttpStatus.OK.value());
        } else {
            response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        response.put("log", cdcResponse.getLog());
        response.put("error", cdcResponse.getErrorMessage());
        
        return response;
    }

    public CDCResponseData register(String username,String email,String password,String data,String profile)throws IOException {
        GSResponse response = cdcAccountsService.register( username, email, password, data, profile);
        return new ObjectMapper().readValue(response.getResponseText(), CDCResponseData.class);
    }
}
