package com.thermofisher.cdcam.services;

import com.gigya.socialize.GSKeyNotFoundException;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.thermofisher.cdcam.builders.GSRequestFactory;
import com.thermofisher.cdcam.enums.aws.CdcamSecrets;
import com.thermofisher.cdcam.enums.cdc.APIMethods;
import com.thermofisher.cdcam.enums.cdc.AccountType;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccountV2;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.dto.LiteAccountDTO;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.CDCUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class GigyaApi {
    private String mainCdcSecretKey;
    private String secondaryDCSecretKey;

    @Value("${cdc.secondary.datacenter}")
    private String secondaryApiDomain;

    @Value("${cdc.main.apiKey}")
    private String mainApiKey;

    @Value("${cdc.main.datacenter}")
    private String mainApiDomain;

    @Value("${cdc.secondary.apiKey}")
    private String secondaryApiKey;

    @Value("${env.name}")
    private String env;

    @Autowired
    SecretsService secretsService;

    @PostConstruct
    public void setCredentials() {
        try {
            if (env.equals("local") || env.equals("test")) return;

            log.info("Setting up CDC credentials.");
            mainCdcSecretKey = secretsService.get(CdcamSecrets.MAIN_DC.getKey());

            if (CDCUtils.isSecondaryDCSupported(env)) {
                log.info("Setting up Secondary DC Credentials");
                secondaryDCSecretKey = secretsService.get(CdcamSecrets.SECONDARY_DC.getKey());
            }
        } catch (Exception e) {
            log.error(String.format("An error occurred while configuring CDC credentials. Error: %s", Utils.stackTraceToString(e)));
        }
    }

    public GSResponse changePassword(String uid, String newPassword, String oldPassword) {
        final String setAccountInfo = APIMethods.SET_ACCOUNT_INFO.getValue();

        GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, setAccountInfo);
        request.setParam("UID", uid);
        request.setParam("newPassword", newPassword);
        request.setParam("password", oldPassword);
        request.setParam("data", "{\"requirePasswordCheck\": false}");

        return request.send();
    }

    public GSResponse getAccount(String uid) {
        try {
            String apiMethod = APIMethods.GET.getValue();
            log.info(String.format("%s triggered. UID: %s", apiMethod, uid));

            GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
            request.setParam("UID", uid);
            request.setParam("include", "emails,profile,data,password,userInfo,regSource,identities");
            request.setParam("extraProfileFields", "username,locale,work");
            return request.send();
        } catch (Exception e) {
            log.error(String.format("An error occurred while retrieving an account. UID: %s. Error: %s", uid, Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse getAccountV2(String uid) {
        try {
            String apiMethod = APIMethods.GET.getValue();
            log.info(String.format("%s triggered. UID: %s", apiMethod, uid));

            GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
            request.setParam("UID", uid);
            request.setParam("include", "emails,profile,data,password,userInfo,preferences,regSource,identities");
            request.setParam("extraProfileFields", "username,locale,work");
            return request.send();
        } catch (Exception e) {
            log.error(String.format("An error occurred while retrieving an account. UID: %s. Error: %s", uid, Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse getJWTPublicKey() {
        String apiMethod = APIMethods.GET_JWT_PUBLIC_KEY.getValue();
        log.info(String.format("%s triggered.", apiMethod));

        GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
        request.setParam("mainApiKey", mainApiKey);
        return request.send();
    }

    /**
     * Updates an account's data in CDC.
     *
     * @param params object containing data to update.
     * @return {@code GSResponse} that holds response and error codes from CDC.
     * @see <a href="https://help.sap.com/viewer/8b8d6fffe113457094a17701f63e3d6a/LATEST/en-US/41398a8670b21014bbc5a10ce4041860.html">accounts.setAccountInfo</a>
     */
    public GSResponse setAccountInfo(GSObject params) {
        String apiMethod = APIMethods.SET_ACCOUNT_INFO.getValue();
        GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
        request.setParams(params);
        return request.send();
    }

    public GSResponse finalizeRegistration(String regToken) {
        String apiMethod = APIMethods.FINALIZE_REGISTRATION.getValue();
        GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
        request.setParam("regToken", regToken);
        return request.send();
    }

    public GSResponse setAccountInfo(CDCAccount account) {
        String apiMethod = APIMethods.SET_ACCOUNT_INFO.getValue();
        GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);

        JsonObject accountJson = new Gson().toJsonTree(account).getAsJsonObject();
        for (String key : accountJson.keySet()) {
            String value;

            if (accountJson.get(key).isJsonObject() || accountJson.get(key).isJsonArray()) {
                value = accountJson.get(key).toString();
            } else {
                value = accountJson.get(key).getAsString();
            }

            request.setParam(key, value);
        }

        log.info(String.format("%s triggered. UID: %s", apiMethod, account.getUID()));
        return request.send();
    }

    public GSResponse setUserInfo(String uid, String data, String profile, String removeLoginEmails, String username) {
        String apiMethod = APIMethods.SET_ACCOUNT_INFO.getValue();
        log.info(String.format("%s triggered. UID: %s", apiMethod, uid));

        GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
        request.setParam("UID", uid);
        request.setParam("data", data);
        request.setParam("profile", profile);
        if (!Utils.isNullOrEmpty(removeLoginEmails)) {
            request.setParam("removeLoginEmails", removeLoginEmails);
        }
        if (!Utils.isNullOrEmpty(username)) {
            request.setParam("username", username);
        }
        return request.send();
    }

    public GSResponse changeAccountStatus(String uid, boolean status) {
        try {
            String apiMethod = APIMethods.SET_ACCOUNT_INFO.getValue();
            log.info(String.format("%s triggered. UID: %s", apiMethod, uid));

            GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
            request.setParam("UID", uid);
            request.setParam("isActive", status);
            return request.send();
        } catch (Exception e) {
            log.error(String.format("An error occurred while changing the account status. UID: %s. Error: %s", uid, Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse registerLiteAccount(LiteAccountDTO liteAccountDTO) throws CustomGigyaErrorException, GSKeyNotFoundException, JSONException {
        final boolean isLite = true;
        GSResponse initRegResponse = initRegistration(isLite);
        if (CDCUtils.isErrorResponse(initRegResponse)) {
            log.error(String.format("[CDC ERROR] - Error on accounts.initRegistration. Code: %d", initRegResponse.getErrorCode()));
            log.error(String.format("[CDC ERROR] - Log: %s", initRegResponse.getLog()));
            log.error(String.format("[CDC ERROR] - Error message: %s", initRegResponse.getErrorMessage()));
            log.error(String.format("[CDC ERROR] - Error details: %s", initRegResponse.getErrorDetails()));
            log.error(String.format("[CDC ERROR] - Response text: %s", initRegResponse.getResponseText()));
            throw new CustomGigyaErrorException("Error during lite registration. Error code: " + initRegResponse.getErrorCode());
        }

        GSObject data = initRegResponse.getData();
        String apiMethod = APIMethods.SET_ACCOUNT_INFO.getValue();

        GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
        request.setParam("regToken", getRegToken(data));
        request.setParam("profile", generateProfileJson(liteAccountDTO));
        request.setParam("data", generateDataJson(liteAccountDTO));

        return request.send();
    }

    public GSResponse registerLiteAccount(String email) throws GSKeyNotFoundException, CustomGigyaErrorException {
        final boolean isLite = true;
        GSResponse initRegResponse = initRegistration(isLite);
        if (CDCUtils.isErrorResponse(initRegResponse)) {
            log.error(String.format("[CDC ERROR] - Error on accounts.initRegistration. Code: %d", initRegResponse.getErrorCode()));
            log.error(String.format("[CDC ERROR] - Log: %s", initRegResponse.getLog()));
            log.error(String.format("[CDC ERROR] - Error message: %s", initRegResponse.getErrorMessage()));
            log.error(String.format("[CDC ERROR] - Error details: %s", initRegResponse.getErrorDetails()));
            log.error(String.format("[CDC ERROR] - Response text: %s", initRegResponse.getResponseText()));
            throw new CustomGigyaErrorException("Error during lite registration. Error code: " + initRegResponse.getErrorCode());
        }

        GSObject data = initRegResponse.getData();
        String apiMethod = APIMethods.SET_ACCOUNT_INFO.getValue();

        GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
        request.setParam("regToken", getRegToken(data));
        request.setParam("profile", String.format("{\"email\":\"%s\"}", email));

        return request.send();
    }

    public GSResponse search(String query, AccountType accountType, String apiDomain) {
        String apiMethod = APIMethods.SEARCH.getValue();
        log.info(String.format("%s triggered. Query: %s", apiMethod, query));

        GSRequest request = buildGSRequest(apiMethod, apiDomain);
        request.setParam("accountTypes", accountType.getValue());
        request.setParam("query", query);

        String context = String.format("search/%s", UUID.randomUUID().toString());
        request.setParam("context", context);
        log.info(String.format("%s called. Context: %s", apiMethod, context));

        return request.send();
    }

    public GSResponse register(CDCNewAccount newAccount) {
        try {
            String apiMethod = APIMethods.REGISTER.getValue();
            log.info(String.format("%s triggered. Username: %s", apiMethod, newAccount.getUsername()));

            GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
            request.setParam("username", newAccount.getUsername());
            request.setParam("email", newAccount.getEmail());
            request.setParam("password", newAccount.getPassword());
            request.setParam("data", newAccount.getData());
            request.setParam("profile", newAccount.getProfile());
            request.setParam("regSource", "tf-registration");
            request.setParam("finalizeRegistration", "true");
            return request.send();
        } catch (Exception e) {
            log.error(String.format("An error occurred while creating account. Username: %s. Error: %s", newAccount.getUsername(), Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse register(CDCNewAccountV2 newAccount) {
        try {
            String apiMethod = APIMethods.REGISTER.getValue();
            log.info(String.format("%s triggered. Username: %s", apiMethod, newAccount.getUsername()));

            GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
            request.setParam("username", newAccount.getUsername());
            request.setParam("email", newAccount.getEmail());
            request.setParam("password", newAccount.getPassword());
            request.setParam("data", newAccount.getData());
            request.setParam("profile", newAccount.getProfile());
            request.setParam("preferences", newAccount.getPreferences());
            request.setParam("regSource", "tf-registration");
            request.setParam("finalizeRegistration", "true");
            return request.send();
        } catch (Exception e) {
            log.error(String.format("An error occurred while creating account. Username: %s. Error: %s", newAccount.getUsername(), Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse sendVerificationEmail(String uid) {
        try {
            String apiMethod = APIMethods.SEND_VERIFICATION_EMAIL.getValue();
            log.info(String.format("%s triggered. UID: %s", apiMethod, uid));

            GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
            request.setParam("UID", uid);

            return request.send();
        } catch (Exception e) {
            log.error(String.format("An error occurred while sending the verification email to the user. UID: %s. Error: %s", uid, Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse resetPassword(GSObject params) {
        try {
            String apiMethod = APIMethods.RESET_PASSWORD.getValue();
            log.info(String.format("%s triggered.", apiMethod));
            GSRequest request = GSRequestFactory.createWithParams(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod, params);
            return request.send();
        } catch (Exception e) {
            log.error(String.format("An error occurred while calling the reset password endpoint . Error: %s", Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse isAvailableLoginId(String loginId, String apiDomain) {
        String apiMethod = APIMethods.IS_AVAILABLE_LOGINID.getValue();
        GSRequest request = buildGSRequest(apiMethod, apiDomain);
        request.setParam("loginID", loginId);

        String context = String.format("isAvailableLoginId/%s", UUID.randomUUID().toString());
        request.setParam("context", context);
        log.info(String.format("%s called. Context: %s", apiMethod, context));

        return request.send();
    }

    public GSResponse getRP(String clientId) {
        final String getRP = APIMethods.GET_RP.getValue();

        GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, getRP);
        request.setParam("clientID", clientId);

        return request.send();
    }

    public GSResponse updateRequirePasswordCheck(String uid) {
        String apiMethod = APIMethods.SET_ACCOUNT_INFO.getValue();
        log.info(String.format("%s triggered. UID: %s", apiMethod, uid));

        GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
        request.setParam("UID", uid);
        request.setParam("data", "{\"requirePasswordCheck\": false}");
        return request.send();
    }

    private GSRequest buildGSRequest(String apiMethod, String apiDomain) {
        GSRequest request;
        if (isMainApiDomain(apiDomain)) {
            log.info("CDC request built for main domain");
            request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, apiDomain, apiMethod);
        } else {
            log.info("CDC request built for secondary domain");
            request = GSRequestFactory.create(secondaryApiKey, secondaryDCSecretKey, apiDomain, apiMethod);
        }
        return request;
    }

    private String generateDataJson(LiteAccountDTO liteAccountDTO) throws JSONException {
        JSONObject dataJson = new JSONObject();
        if (!Utils.isNullOrEmpty(liteAccountDTO.getInviterEmail()))
            dataJson.put("inviterEmail", liteAccountDTO.getInviterEmail());
        if (!Utils.isNullOrEmpty(liteAccountDTO.getClientId())) dataJson.put("clientId", liteAccountDTO.getClientId());
        return dataJson.toString();
    }

    private String generateProfileJson(LiteAccountDTO liteAccountDTO) throws JSONException {
        JSONObject profileJson = new JSONObject();
        profileJson.put("email", liteAccountDTO.getEmail());
        if (!Utils.isNullOrEmpty(liteAccountDTO.getFirstName()))
            profileJson.put("firstName", liteAccountDTO.getFirstName());
        if (!Utils.isNullOrEmpty(liteAccountDTO.getLastName()))
            profileJson.put("lastName", liteAccountDTO.getLastName());
        if (!Utils.isNullOrEmpty(liteAccountDTO.getLocation()))
            profileJson.put("country", liteAccountDTO.getLocation());
        return profileJson.toString();
    }

    private boolean isMainApiDomain(String apiDomain) {
        return apiDomain == null || apiDomain == mainApiDomain;
    }

    private GSResponse initRegistration(boolean isLite) {
        String apiMethod = APIMethods.INIT_REGISTRATION.getValue();
        GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
        request.setParam("isLite", isLite);

        String context = String.format("initRegistration/%s", UUID.randomUUID().toString());
        ;
        request.setParam("context", context);
        log.info(String.format("%s called. Context: %s", apiMethod, context));

        return request.send();
    }

    private String getRegToken(GSObject data) throws GSKeyNotFoundException {
        return data.getString("regToken");
    }
}
