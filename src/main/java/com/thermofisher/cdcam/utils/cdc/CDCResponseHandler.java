package com.thermofisher.cdcam.utils.cdc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSArray;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.model.*;
import com.thermofisher.cdcam.services.CDCAccountsService;
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

    public AccountInfo getAccountInfoByEmail(String email) throws IOException {
        String uid = this.getUIDByEmail(email);
        
        if (uid.isEmpty()) {
            return null;
        };

        return this.getAccountInfo(uid);
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

    public CDCResponseData register(CDCNewAccount newAccount) throws IOException {
        GSResponse response = cdcAccountsService.register(newAccount);
        return new ObjectMapper().readValue(response.getResponseText(), CDCResponseData.class);
    }

    public String searchDuplicatedAccountUid(String uid, String email) throws IOException {
        String query = String.format("SELECT UID,loginIDs FROM accounts WHERE profile.username CONTAINS '%1$s' OR profile.email CONTAINS '%1$s'", email);
        GSResponse response = cdcAccountsService.search(query, "");
        CDCSearchResponse cdcSearchResponse = new ObjectMapper().readValue(response.getResponseText(), CDCSearchResponse.class);

        for (CDCAccount result : cdcSearchResponse.getResults()) {
            if (!uid.equals(result.getUID()) && (result.getLoginIDs().getUsername() != null || result.getLoginIDs().getEmails().length > 0 || result.getLoginIDs().getUnverifiedEmails().length > 0)) {
                return result.getUID();
            }
        }
        logger.warn(String.format("Could not match an account with that email on CDC. UID: %s. Error: %s", uid, response.getErrorMessage()));
        return NO_RESULTS_FOUND;
    }

    public boolean disableAccount(String uid) {
        boolean SUCCESSFUL_UPDATE = true;
        boolean UNSUCCESSFUL_UPDATE = false;

        logger.info(String.format("A process has started to change the account status for a user. UID: %s", uid));
        GSResponse changeStatusResponse = cdcAccountsService.changeAccountStatus(uid, false);

        if (changeStatusResponse.getErrorCode() == 0) {
            return SUCCESSFUL_UPDATE;
        }
        logger.error(String.format("An error occurred while changing the account status. UID: %s. Error: %s", uid, changeStatusResponse.getErrorMessage()));
        return UNSUCCESSFUL_UPDATE;
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

    public String getEmailByUsername(String userName) throws IOException {
        String query = String.format("SELECT emails,loginIDs FROM accounts WHERE profile.username CONTAINS '%1$s'", userName);
        GSResponse response = cdcAccountsService.search(query, "");
        CDCSearchResponse cdcSearchResponse = new ObjectMapper().readValue(response.getResponseText(), CDCSearchResponse.class);

        for (CDCAccount result : cdcSearchResponse.getResults()) {
            if (result.getEmails().getVerified().size() > 0)
                return result.getEmails().getVerified().get(0);
            else if (result.getEmails().getUnverified().size() > 0)
                return result.getEmails().getUnverified().get(0);
            else
                return NO_RESULTS_FOUND;
        }
        logger.warn(String.format("Could not match an account with that username on CDC. username: %s. Error: %s", userName, response.getErrorMessage()));
        return NO_RESULTS_FOUND;
    }

    public String getUsernameByEmail(String email) {
        String username = "";
        try {
            String query = String.format("select profile.username from accounts where emails.verified contains '%1$s' or emails.unverified contains '%1$s'", email);
            GSResponse response = cdcAccountsService.search(query, "");

            if (response.getErrorCode() == 0) {
                GSObject obj = response.getData();
                GSArray results = obj.getArray("results");
                for (Object result : results) {
                    GSObject profile = (GSObject) ((GSObject) result).get("profile");
                    username = profile.containsKey("username") ? profile.getString("username") : NO_RESULTS_FOUND;
                    return username;
                }
            }
            logger.warn(String.format("Could not match an account with the email on CDC. email: %s. Error: %s", email, response.getErrorMessage()));
        } catch (Exception e) {
           logger.error(Utils.stackTraceToString(e));
        }
        return username;
    }

    public String getUIDByEmail(String email) throws IOException {
        String query = String.format("select UID from accounts where emails.verified contains '%1$s' or emails.unverified contains '%1$s'", email);
        GSResponse response = cdcAccountsService.search(query, "");
        CDCSearchResponse cdcSearchResponse = new ObjectMapper().readValue(response.getResponseText(), CDCSearchResponse.class);

        for (CDCAccount result : cdcSearchResponse.getResults()) {
            return  result.getUID();
        }
        logger.warn(String.format("Could not match an account with that email on CDC. email: %s. Error: %s", email, response.getErrorMessage()));
        return NO_RESULTS_FOUND;
    }

    public String getUIDByUsername(String userName) throws IOException {
        String query = String.format("SELECT UID FROM accounts WHERE profile.username CONTAINS '%1$s'", userName);
        GSResponse response = cdcAccountsService.search(query, "");
        CDCSearchResponse cdcSearchResponse = new ObjectMapper().readValue(response.getResponseText(), CDCSearchResponse.class);

        for (CDCAccount result : cdcSearchResponse.getResults()) {
            return  result.getUID();
        }
        logger.warn(String.format("Could not match an account with that username on CDC. username: %s. Error: %s", userName, response.getErrorMessage()));
        return NO_RESULTS_FOUND;
    }

    public boolean resetPasswordRequest(String username) {
        ResetPassword resetPasswordRequest = ResetPassword.builder().username(username).build();
        GSResponse response = cdcAccountsService.resetPasswordRequest(resetPasswordRequest);
        if (response.getErrorCode() == 0)
            return true;
        else {
            logger.error(response.getErrorMessage());
            return false;
        }
    }

    public ResetPasswordResponse resetPassword(ResetPassword resetPassword) {
        GSResponse gsResponse = cdcAccountsService.resetPasswordRequest(resetPassword);

        return ResetPasswordResponse.builder()
                .responseCode(gsResponse.getErrorCode())
                .responseMessage(gsResponse.getErrorMessage()).build();
    }
}
