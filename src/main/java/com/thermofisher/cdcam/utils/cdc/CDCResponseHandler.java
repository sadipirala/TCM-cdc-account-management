package com.thermofisher.cdcam.utils.cdc;

import java.io.IOException;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
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
import com.thermofisher.cdcam.enums.cdc.DataCenter;
import com.thermofisher.cdcam.enums.cdc.GigyaCodes;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.ResetPasswordResponse;
import com.thermofisher.cdcam.model.ResetPasswordSubmit;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccountV2;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CDCSearchResponse;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.JWTPublicKey;
import com.thermofisher.cdcam.model.cdc.LoginIdDoesNotExistException;
import com.thermofisher.cdcam.model.cdc.OpenIdRelyingParty;
import com.thermofisher.cdcam.model.cdc.SearchResponse;
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

    @Value("${cdc.main.datacenter.name}")
    private String mainDataCenter;

    @Value("${cdc.secondary.datacenter}")
    private String secondaryApiDomain;

    @Value("${cdc.secondary.datacenter.name}")
    private String secondaryDataCenter;

    @Value("${is-new-marketing-enabled}")
    private Boolean isNewMarketingConsentEnabled;

    @Value("${env.name}")
    private String env;

    public void changePassword(String uid, String newPassword, String oldPassword) throws CustomGigyaErrorException {
        GSResponse gsResponse = cdcAccountsService.changePassword(uid, newPassword, oldPassword);

        if (isErrorResponse(gsResponse)) {
            String error = String.format("%s, %s. Error code: %d", gsResponse.getErrorMessage(), gsResponse.getErrorDetails(), gsResponse.getErrorCode());
            throw new CustomGigyaErrorException(error);
        }
    }

    public AccountInfo getAccountInfo(String uid) throws CustomGigyaErrorException {
        if (isNewMarketingConsentEnabled) {
            return getAccountInfoV2(uid);
        }
        return getAccountInfoV1(uid);
    }

    private AccountInfo getAccountInfoV1(String uid) throws CustomGigyaErrorException {
        GSResponse gsResponse = cdcAccountsService.getAccount(uid);
        if (gsResponse.getErrorCode() == 0) {
            GSObject obj = gsResponse.getData();
            return accountBuilder.getAccountInfo(obj);
        } else {
            String error = String.format("An error occurred while retrieving account info. UID: %s. Error details: %s Error code: %s", uid, gsResponse.getErrorDetails(), gsResponse.getErrorCode());
            throw new CustomGigyaErrorException(error, gsResponse.getErrorCode());
        }
    }

    private AccountInfo getAccountInfoV2(String uid) throws CustomGigyaErrorException {
        GSResponse gsResponse = cdcAccountsService.getAccountV2(uid);
        if (gsResponse.getErrorCode() == 0) {
            GSObject obj = gsResponse.getData();
            return accountBuilder.getAccountInfoV2(obj);
        } else {
            String error = String.format("An error occurred while retrieving account info. UID: %s. Error details: %s Error code: %s", uid, gsResponse.getErrorDetails(), gsResponse.getErrorCode());
            throw new CustomGigyaErrorException(error, gsResponse.getErrorCode());
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
        if (cdcResponse.getErrorCode() == GigyaCodes.SUCCESS.getValue()) {
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
        GSResponse gsResponse = cdcAccountsService.register(newAccount);
        return new ObjectMapper().readValue(gsResponse.getResponseText(), CDCResponseData.class);
    }

    public CDCResponseData register(CDCNewAccountV2 newAccount) throws IOException {
        GSResponse gsResponse = cdcAccountsService.register(newAccount);
        return new ObjectMapper().readValue(gsResponse.getResponseText(), CDCResponseData.class);
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
        GSResponse gsResponse = cdcAccountsService.sendVerificationEmail(uid);
        CDCResponseData cdcResponseData = new CDCResponseData();

        if (gsResponse != null) {
            ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
            cdcResponseData = mapper.readValue(gsResponse.getResponseText(), CDCResponseData.class);
        } else {
            cdcResponseData.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return cdcResponseData;
    }

    public String getEmailByUsername(String userName) throws IOException {
        String query = String.format("SELECT emails,loginIDs FROM accounts WHERE profile.username CONTAINS '%1$s'", userName);
        GSResponse gsResponse = cdcAccountsService.search(query, AccountType.FULL, mainApiDomain);
        CDCSearchResponse cdcSearchResponse = new ObjectMapper().readValue(gsResponse.getResponseText(), CDCSearchResponse.class);

        for (CDCAccount result : cdcSearchResponse.getResults()) {
            if (result.getEmails().getVerified().size() > 0)
                return result.getEmails().getVerified().get(0);
            else if (result.getEmails().getUnverified().size() > 0)
                return result.getEmails().getUnverified().get(0);
            else
                return NO_RESULTS_FOUND;
        }
        logger.warn(String.format("Could not match an account with that username on CDC. username: %s. Error: %s", userName, gsResponse.getErrorMessage()));
        return NO_RESULTS_FOUND;
    }

    public String getUsernameByEmail(String email) {
        String username = "";
        try {
            String query = String.format("select profile.username from accounts where emails.verified contains '%1$s' or emails.unverified contains '%1$s'", email);
            GSResponse gsResponse = cdcAccountsService.search(query, AccountType.FULL, mainApiDomain);

            if (gsResponse.getErrorCode() == 0) {
                GSObject obj = gsResponse.getData();
                GSArray results = obj.getArray("results");
                for (Object result : results) {
                    GSObject profile = (GSObject) ((GSObject) result).get("profile");
                    username = profile.containsKey("username") ? profile.getString("username") : NO_RESULTS_FOUND;
                    return username;
                }
            }
            logger.warn(String.format("Could not match an account with the email on CDC. email: %s. Error: %s", email, gsResponse.getErrorMessage()));
        } catch (Exception e) {
            logger.error(Utils.stackTraceToString(e));
        }
        return username;
    }

    public String getUIDByEmail(String email) throws IOException {
        String query = String.format("select UID from accounts where emails.verified contains '%1$s' or emails.unverified contains '%1$s'", email);
        GSResponse gsResponse = cdcAccountsService.search(query, AccountType.FULL, mainApiDomain);
        CDCSearchResponse cdcSearchResponse = new ObjectMapper().readValue(gsResponse.getResponseText(), CDCSearchResponse.class);

        for (CDCAccount result : cdcSearchResponse.getResults()) {
            return result.getUID();
        }
        logger.warn(String.format("Could not match an account with that email on CDC. email: %s. Error: %s", email, gsResponse.getErrorMessage()));
        return NO_RESULTS_FOUND;
    }

    public String resetPasswordRequest(String username) throws CustomGigyaErrorException, LoginIdDoesNotExistException, GSKeyNotFoundException {
        logger.info(String.format("Reset password request triggered for username: %s.", username));
        GSObject requestParams = new GSObject();
        requestParams.put("loginID", username);
        requestParams.put("sendEmail", SEND_EMAIL);
        GSResponse gsResponse = cdcAccountsService.resetPassword(requestParams);

        if (gsResponse.getErrorCode() == GigyaCodes.LOGIN_ID_DOES_NOT_EXIST.getValue()) {
            String message = String.format("LoginID: %s does not exist.", username);
            throw new LoginIdDoesNotExistException(message);
        } else if (gsResponse.getErrorCode() != GigyaCodes.SUCCESS.getValue()) {
            String errorMessage = String.format("Failed to send a reset password request for %s. CDC error code: %s", username, gsResponse.getErrorCode());
            throw new CustomGigyaErrorException(errorMessage);
        }

        GSObject data = gsResponse.getData();
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
        GSResponse gsResponse = cdcAccountsService.isAvailableLoginId(loginId, apiDomain);

        logger.info(String.format("Error code: %d", gsResponse.getErrorCode()));
        if (isErrorResponse(gsResponse)) {
            String errorMessage = String.format("Error on isAvailableLoginID::%s: %s - %s", apiDomain, gsResponse.getErrorCode(), gsResponse.getErrorMessage());
            throw new CustomGigyaErrorException(errorMessage);
        }

        GSObject gsObject = gsResponse.getData();
        return gsObject.getBool(IS_AVAILABLE_PARAM);
    }

    public IdentityProviderResponse getIdPInformation(String idpName) {
        GSResponse gsResponse = cdcIdentityProviderService.getIdPInformation(idpName);

        if (gsResponse.getErrorCode() == 0) {
            GSObject obj = gsResponse.getData();
            return identityProviderBuilder.getIdPInformation(obj);
        } else {
            logger.error(String.format("An error occurred while retrieving IdP info. IdP Name: %s. Error: %s", idpName, gsResponse.getErrorDetails()));
            return null;
        }
    }

    public JWTPublicKey getJWTPublicKey() throws CustomGigyaErrorException, GSKeyNotFoundException {
        logger.info("Getting JWTPublicKey from CDC.");
        GSResponse gsResponse = cdcAccountsService.getJWTPublicKey();
        logger.info("Got JWTPublicKey.");

        if (isErrorResponse(gsResponse)) {
            String error = String.format("Error on getJWTPublicKey. Error code: %d. Message: %s.", gsResponse.getErrorCode(), gsResponse.getErrorMessage());
            logger.error(error);
            throw new CustomGigyaErrorException(error);
        }
        
        GSObject gsData = gsResponse.getData();
        JWTPublicKey jwtPublicKey = JWTPublicKey.builder()
            .n(gsData.getString("n"))
            .e(gsData.getString("e"))
            .build();
        return jwtPublicKey;
    }

    public CDCSearchResponse search(String query, AccountType accountType, String apiDomain) throws CustomGigyaErrorException, IOException {
        GSResponse gsResponse = cdcAccountsService.search(query, accountType, apiDomain);

        if (isErrorResponse(gsResponse)) {
            throw new CustomGigyaErrorException(gsResponse.getErrorMessage(), gsResponse.getErrorCode());
        }

        return new ObjectMapper().readValue(gsResponse.getResponseText(), CDCSearchResponse.class);
    }

    public SearchResponse searchInBothDC(String email) throws CustomGigyaErrorException, IOException {
        SearchResponse response = SearchResponse.builder().build();
        String query = String.format("SELECT * FROM accounts WHERE profile.username CONTAINS '%1$s' OR profile.email CONTAINS '%1$s'", email);
        
        CDCSearchResponse cdcSearchResponse = this.search(query, AccountType.FULL_LITE, mainApiDomain);
        response.setCdcSearchResponse(cdcSearchResponse);

        boolean accountFound = cdcSearchResponse.getResults().size() > 0;
        if (accountFound) {
            response.setDataCenter(DataCenter.getEqualsAs(mainDataCenter));
            return response;
        }
        
        if (CDCUtils.isSecondaryDCSupported(env)) {
            cdcSearchResponse = this.search(query, AccountType.FULL_LITE, secondaryApiDomain);
            response.setCdcSearchResponse(cdcSearchResponse);
            
            accountFound = cdcSearchResponse.getResults().size() > 0;
            if (accountFound) {
                response.setDataCenter(DataCenter.getEqualsAs(secondaryDataCenter));
            }
        }

        return response;
    }

    public CDCResponseData registerLiteAccount(String email) throws IOException, CustomGigyaErrorException, GSKeyNotFoundException {
        GSResponse gsResponse = cdcAccountsService.registerLiteAccount(email);
        CDCResponseData cdcResponseData = new ObjectMapper().readValue(gsResponse.getResponseText(), CDCResponseData.class);

        if (isErrorResponse(gsResponse)) {
            String errorList = Utils.convertJavaToJsonString(cdcResponseData.getValidationErrors());
            String errorDetails = String.format("%s: %s", gsResponse.getErrorMessage(), errorList);
            throw new CustomGigyaErrorException(errorDetails, gsResponse.getErrorCode());
        };

        return cdcResponseData;
    }

    public OpenIdRelyingParty getRP(String clientId) throws CustomGigyaErrorException, GSKeyNotFoundException {
        GSResponse gsResponse = cdcAccountsService.getRP(clientId);
        if (isErrorResponse(gsResponse)) {
            String error = String.format("Error on getRP. Error code: %d. Message: %s.", gsResponse.getErrorCode(), gsResponse.getErrorMessage());
            logger.error(error);
            throw new CustomGigyaErrorException(error);
        }

        logger.info(String.format("Getting data for clientId: %s", clientId));
        GSObject gsData = gsResponse.getData();
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

    public void updateRequirePasswordCheck(String uid) throws CustomGigyaErrorException {
        GSResponse gsResponse = cdcAccountsService.updateRequirePasswordCheck(uid);
        if (isErrorResponse(gsResponse)) {
            String error = String.format("Error updating requirePasswordCheck. Error code: %d. Error Message: %s.", gsResponse.getErrorCode(), gsResponse.getErrorMessage());
            throw new CustomGigyaErrorException(error);
        };
    }

    public void setAccountInfo(CDCAccount cdcAccount) throws CustomGigyaErrorException {
        GSResponse gsResponse = cdcAccountsService.setAccountInfo(cdcAccount);

        if (isErrorResponse(gsResponse)) {
            throw new CustomGigyaErrorException(gsResponse.getErrorMessage());
        }
    }

    private boolean isErrorResponse(GSResponse gsResponse) {
        return gsResponse.getErrorCode() != GigyaCodes.SUCCESS.getValue();
    }
}
