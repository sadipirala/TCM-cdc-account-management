package com.thermofisher.cdcam.utils.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.model.CDCAccount;
import com.thermofisher.cdcam.model.CDCResponseData;
import com.thermofisher.cdcam.model.CDCSearchResponse;
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
    private final AccountBuilder accountBuilder = new AccountBuilder();

    private Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    CDCAccountsService cdcAccountsService;

    public AccountInfo getAccountInfo(String uid) {
        GSResponse response = cdcAccountsService.getAccount(uid);
        if (response.getErrorCode() == 0) {
            GSObject obj = response.getData();
            return accountBuilder.getAccountInfo(obj);
        } else {
            logger.error(String.format("An error occurred while retrieving account info. UID: %s. Error: %s", uid, response.getErrorDetails()));
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
            logger.error(String.format("An error occurred while updating an account. UID: %s. Error: %s", uid, cdcResponse.getErrorMessage()));
        }
        response.put("log", cdcResponse.getLog());
        response.put("error", cdcResponse.getErrorMessage());
        
        return response;
    }

    public CDCResponseData register(String username,String email,String password,String data,String profile)throws IOException {
        GSResponse response = cdcAccountsService.register( username, email, password, data, profile);
        return new ObjectMapper().readValue(response.getResponseText(), CDCResponseData.class);
    }

    public boolean disableDuplicatedAccounts(String uid, String federatedEmail) {

        boolean SUCCESSFUL_UPDATE = true;
        boolean UNSUCCESSFUL_UPDATE = false;
        String query = String.format("SELECT * FROM accounts WHERE profile.username = '%1$s' OR profile.email = '%1$s'", federatedEmail);
        GSResponse response = cdcAccountsService.search(query, "");
        try {
            CDCSearchResponse cdcSearchResponse = new ObjectMapper().readValue(response.getResponseText(), CDCSearchResponse.class);
            if (cdcSearchResponse.getErrorCode() == 0) {
                if (cdcSearchResponse.getTotalCount() > 0) {
                    for (CDCAccount result : cdcSearchResponse.getResults()) {
                        if (!uid.equals(result.getUID())) {
                            GSResponse changeStatusResponse = cdcAccountsService.changeAccountStatus(result.getUID(), false);
                                if (changeStatusResponse.getErrorCode() == SUCCESS_CODE) {
                                    logger.info(String.format("Account status successfully changed. UID: %s", uid));
                                } else {
                                    logger.error(String.format("An error occurred while updating an account status. UID: %s. Error: %s", uid, changeStatusResponse.getErrorMessage()));
                                    return UNSUCCESSFUL_UPDATE;
                                }
                        }
                    }
                    return SUCCESSFUL_UPDATE;
                }
                else {
                    logger.error(String.format("Could not match an account with that email on CDC. Email: %s. Error: %s", federatedEmail, response.getErrorMessage()));
                    return UNSUCCESSFUL_UPDATE;
                }
            }
            else {
                logger.error(String.format("An error occurred while searching in CDC. UID: %s. Error: %s", uid, response.getErrorMessage()));
                return UNSUCCESSFUL_UPDATE;
            }
        }
        catch(Exception e) {
            logger.error(String.format("An error occurred while processing an account status update request. Error: %s", Utils.stackTraceToString(e)));
            return UNSUCCESSFUL_UPDATE;
        }
    }
}
