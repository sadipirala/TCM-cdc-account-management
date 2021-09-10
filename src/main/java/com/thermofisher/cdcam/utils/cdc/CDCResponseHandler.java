package com.thermofisher.cdcam.utils.cdc;

import java.io.IOException;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gigya.socialize.GSArray;
import com.gigya.socialize.GSKeyNotFoundException;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.builders.IdentityProviderBuilder;
import com.thermofisher.cdcam.enums.cdc.AccountType;
import com.thermofisher.cdcam.enums.cdc.GigyaCodes;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.ResetPasswordResponse;
import com.thermofisher.cdcam.model.ResetPasswordSubmit;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CDCSearchResponse;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.JWTPublicKey;
import com.thermofisher.cdcam.model.cdc.LoginIdDoesNotExistException;
import com.thermofisher.cdcam.model.cdc.OpenIdRelyingParty;
import com.thermofisher.cdcam.model.identityProvider.IdentityProviderResponse;
import com.thermofisher.cdcam.services.CDCAccountsService;
import com.thermofisher.cdcam.services.CDCIdentityProviderService;
import com.thermofisher.cdcam.utils.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * CDCAccountsService
 */
@Service
public class CDCResponseHandler {
    private final int SUCCESS_CODE = 0;
    private final String NO_RESULTS_FOUND = "";
    private final AccountBuilder accountBuilder = new AccountBuilder();
    private final IdentityProviderBuilder identityProviderBuilder = new IdentityProviderBuilder();
    private final boolean SEND_EMAIL = false;

    private Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    CDCAccountsService cdcAccountsService;

    @Autowired(required = false)
    CDCIdentityProviderService cdcIdentityProviderService;

    @Value("${cdc.main.datacenter}")
    private String mainApiDomain;

    @Value("${cdc.secondary.datacenter}")
    private String secondaryApiDomain;

    @Value("${env.name}")
    private String env;

    public void changePassword(String uid, String newPassword, String oldPassword) throws CustomGigyaErrorException {
        GSResponse response = cdcAccountsService.changePassword(uid, newPassword, oldPassword);

        if (response.getErrorCode() != 0) {
            String error = String.format("%s, %s. Error code: %d", response.getErrorMessage(), response.getErrorDetails(), response.getErrorCode());
            throw new CustomGigyaErrorException(error);
        }
    }

    public AccountInfo getAccountInfo(String uid) throws CustomGigyaErrorException {
        GSResponse response = cdcAccountsService.getAccount(uid);
        if (response.getErrorCode() == 0) {
            GSObject obj = response.getData();
            return accountBuilder.getAccountInfo(obj);
        } else {
            String error = String.format("An error occurred while retrieving account info. UID: %s. Error: %s", uid, response.getErrorDetails());
            throw new CustomGigyaErrorException(error);
        }
    }

    public AccountInfo getAccountInfoByEmail(String email) throws IOException, CustomGigyaErrorException {
        String uid = this.getUIDByEmail(email);

        if (uid.isEmpty()) {
            return null;
        }

        return this.getAccountInfo(uid);
    }

    public ObjectNode update(JSONObject account) {
        if (account == null) return null;
        String uid = Utils.getStringFromJSON(account, "uid");
        JSONObject data = Utils.getObjectFromJSON(account, "data");
        JSONObject profile = Utils.getObjectFromJSON(account, "profile");
        String removeLoginEmails = Utils.getStringFromJSON(account, "removeLoginEmails");
        String username = Utils.getStringFromJSON(account, "username");

        String dataJson = (data != null) ? data.toString() : "";
        String profileJson = (profile != null) ? profile.toString() : "";
        String removeLoginEmailsJson = (removeLoginEmails != null) ? removeLoginEmails : "";
        String usernameJson = (username != null) ? username : "";

        GSResponse cdcResponse = cdcAccountsService.setUserInfo(uid, dataJson, profileJson, removeLoginEmailsJson, usernameJson);

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
        GSResponse response = cdcAccountsService.search(query, AccountType.FULL);
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
            GSResponse response = cdcAccountsService.search(query, AccountType.FULL);

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
        GSResponse response = cdcAccountsService.search(query, AccountType.FULL);
        CDCSearchResponse cdcSearchResponse = new ObjectMapper().readValue(response.getResponseText(), CDCSearchResponse.class);

        for (CDCAccount result : cdcSearchResponse.getResults()) {
            return result.getUID();
        }
        logger.warn(String.format("Could not match an account with that email on CDC. email: %s. Error: %s", email, response.getErrorMessage()));
        return NO_RESULTS_FOUND;
    }

    public String resetPasswordRequest(String username) throws CustomGigyaErrorException, LoginIdDoesNotExistException, GSKeyNotFoundException {
        logger.info(String.format("Reset password request triggered for username: %s.", username));
        GSObject requestParams = new GSObject();
        requestParams.put("loginID", username);
        requestParams.put("sendEmail", SEND_EMAIL);
        GSResponse response = cdcAccountsService.resetPassword(requestParams);

        if (response.getErrorCode() == GigyaCodes.LOGIN_ID_DOES_NOT_EXIST.getValue()) {
            String message = String.format("LoginID: %s does not exist.", username);
            throw new LoginIdDoesNotExistException(message);
        } else if (response.getErrorCode() != GigyaCodes.SUCCESS.getValue()) {
            String errorMessage = String.format("Failed to send a reset password request for %s. CDC error code: %s", username, response.getErrorCode());
            throw new CustomGigyaErrorException(errorMessage);
        }

        GSObject data = response.getData();
        return data.getString("passwordResetToken");
    }

    public ResetPasswordResponse resetPasswordSubmit(ResetPasswordSubmit resetPassword) {
        logger.info(String.format("Reset password submit triggered for UID: %s.", resetPassword.getUid()));
        GSObject resetParams = new GSObject();
        resetParams.put("passwordResetToken", resetPassword.getResetPasswordToken());
        resetParams.put("newPassword", resetPassword.getNewPassword());

        GSResponse gsResponse = cdcAccountsService.resetPassword(resetParams);

        return ResetPasswordResponse
            .builder()
            .responseCode(gsResponse.getErrorCode())
            .responseMessage(gsResponse.getErrorMessage())
            .build();
    }

    public boolean isAvailableLoginId(String loginId) throws CustomGigyaErrorException, InvalidClassException, GSKeyNotFoundException, NullPointerException {
        boolean isAvailableLoginId = false;

        isAvailableLoginId = isAvailableLoginId(loginId, mainApiDomain);
        if (!isAvailableLoginId) {
            return isAvailableLoginId;
        }

        if (CDCUtils.isSecondaryDCSupported(env)) {
            isAvailableLoginId = isAvailableLoginId(loginId, secondaryApiDomain);
        }

        return isAvailableLoginId;
    }

    private boolean isAvailableLoginId(String loginId, String apiDomain) throws CustomGigyaErrorException, InvalidClassException, GSKeyNotFoundException, NullPointerException {
        final String IS_AVAILABLE_PARAM = "isAvailable";
        GSResponse response = cdcAccountsService.isAvailableLoginId(loginId, apiDomain);

        logger.info(String.format("Error code: %d", response.getErrorCode()));
        if (response.getErrorCode() != 0) {
            String errorMessage = String.format("Error on isAvailableLoginID::%s: %s - %s", apiDomain, response.getErrorCode(), response.getErrorMessage());
            throw new CustomGigyaErrorException(errorMessage);
        }

        GSObject gsObject = response.getData();
        return gsObject.getBool(IS_AVAILABLE_PARAM);
    }

    public IdentityProviderResponse getIdPInformation(String idpName) {
        GSResponse response = cdcIdentityProviderService.getIdPInformation(idpName);

        if (response.getErrorCode() == 0) {
            GSObject obj = response.getData();
            return identityProviderBuilder.getIdPInformation(obj);
        } else {
            logger.error(String.format("An error occurred while retrieving IdP info. IdP Name: %s. Error: %s", idpName, response.getErrorDetails()));
            return null;
        }
    }

    public JWTPublicKey getJWTPublicKey() throws CustomGigyaErrorException, GSKeyNotFoundException {
        logger.info("Getting JWTPublicKey from CDC.");
        GSResponse response = cdcAccountsService.getJWTPublicKey();
        logger.info("Got JWTPublicKey.");

        if (response.getErrorCode() != 0) {
            String error = String.format("Error on getJWTPublicKey. Error code: %d. Message: %s.", response.getErrorCode(), response.getErrorMessage());
            logger.error(error);
            throw new CustomGigyaErrorException(error);
        }
        
        GSObject gsData = response.getData();
        JWTPublicKey jwtPublicKey = JWTPublicKey.builder()
            .n(gsData.getString("n"))
            .e(gsData.getString("e"))
            .build();
        return jwtPublicKey;
    }

    public CDCSearchResponse search(String query, AccountType accountType) throws CustomGigyaErrorException, JsonParseException, JsonMappingException, IOException {
        GSResponse response = cdcAccountsService.search(query, accountType);

        if (response.getErrorCode() != 0) {
            throw new CustomGigyaErrorException(response.getErrorMessage(), response.getErrorCode());
        }

        return new ObjectMapper().readValue(response.getResponseText(), CDCSearchResponse.class);
    }

    public CDCResponseData liteRegisterUser(String email) throws IOException, CustomGigyaErrorException {
        GSResponse response = cdcAccountsService.setLiteReg(email);
        CDCResponseData cdcResponseData = new ObjectMapper().readValue(response.getResponseText(), CDCResponseData.class);
        
        if (response.getErrorCode() != 0) {
            String errorList = Utils.convertJavaToJsonString(cdcResponseData.getValidationErrors());
            String errorDetails = String.format("%s: %s", response.getErrorMessage(), errorList);
            throw new CustomGigyaErrorException(errorDetails, response.getErrorCode());
        };

        return cdcResponseData;
    }

    public OpenIdRelyingParty getRP(String clientId) throws CustomGigyaErrorException, GSKeyNotFoundException {
        GSResponse response = cdcAccountsService.getRP(clientId);
        if (response.getErrorCode() != 0) {
            String error = String.format("Error on getRP. Error code: %d. Message: %s.", response.getErrorCode(), response.getErrorMessage());
            logger.error(error);
            throw new CustomGigyaErrorException(error);
        }

        logger.info(String.format("Getting data for clientId: %s", clientId));
        GSObject gsData = response.getData();
        GSArray gsRedirectUrisArray = gsData.getArray("redirectUris");
        List<String> redirectURIs = new ArrayList<>();
        for (Object uri : gsRedirectUrisArray) {
            redirectURIs.add((String) uri);
        }

        OpenIdRelyingParty openIdRelyingParty = OpenIdRelyingParty.builder()
            .clientId(clientId)
            .description(gsData.getString("description"))
            .redirectUris(redirectURIs)
            .build();

        return openIdRelyingParty;
    }
}
