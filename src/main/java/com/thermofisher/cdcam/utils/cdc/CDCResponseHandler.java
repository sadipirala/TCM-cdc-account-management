package com.thermofisher.cdcam.utils.cdc;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private final String NO_RESULTS_FOUND = "";
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

    public String searchDuplicatedAccountUid(String uid, String email) throws IOException {
        String query = String.format("SELECT UID,loginIDs FROM accounts WHERE profile.username CONTAINS '%1$s' OR profile.email CONTAINS '%1$s'", email);
        GSResponse response = cdcAccountsService.search(query, "");
        CDCSearchResponse cdcSearchResponse = new ObjectMapper().readValue(response.getResponseText(), CDCSearchResponse.class);

            for (CDCAccount result : cdcSearchResponse.getResults()){
                if (!uid.equals(result.getUID()) && (result.getLoginIDs().getUsername()!= null || result.getLoginIDs().getEmails().length > 0 || result.getLoginIDs().getUnverifiedEmails().length > 0)){
                    return result.getUID();
                }
            }
            logger.warn(String.format("Could not match an account with that email on CDC. UID: %s. Error: %s" , uid, response.getErrorMessage()));
            return NO_RESULTS_FOUND;
    }

    public boolean disableAccount(String uid) {
        boolean SUCCESSFULL_UPDATE = true;
        boolean UNSUCCESSFULL_UPDATE = false;

        logger.info(String.format("A process has started to change the account status for a user. UID: %s", uid));
        GSResponse changeStatusResponse = cdcAccountsService.changeAccountStatus(uid, false);

        if (changeStatusResponse.getErrorCode() == 0){
            return  SUCCESSFULL_UPDATE;
        }
        logger.error(String.format("An error occurred while changing the account status. UID: %s. Error: %s" , uid, changeStatusResponse.getErrorMessage()));
        return UNSUCCESSFULL_UPDATE;
    }

    public CDCResponseData sendVerificationEmail(String uid) throws IOException {
        GSResponse response = cdcAccountsService.sendVerificationEmail(uid);
        CDCResponseData cdcResponseData = new CDCResponseData();

        if (response != null) {
            ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
            cdcResponseData = mapper.readValue(response.getResponseText(), CDCResponseData.class);
        } else {
            cdcResponseData.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return cdcResponseData;
    }
}
