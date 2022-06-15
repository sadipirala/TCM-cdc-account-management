package com.thermofisher.cdcam.services;

import javax.annotation.PostConstruct;

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
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.CDCUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GigyaApi {
    private Logger logger = LogManager.getLogger(this.getClass());
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

            logger.info("Setting up CDC credentials.");
            mainCdcSecretKey = secretsService.get(CdcamSecrets.MAIN_DC.getKey());

            if (CDCUtils.isSecondaryDCSupported(env)) {
                logger.info("Setting up Secondary DC Credentials");
                secondaryDCSecretKey = secretsService.get(CdcamSecrets.SECONDARY_DC.getKey());
            }
        } catch (Exception e) {
            logger.error(String.format("An error occurred while configuring CDC credentials. Error: %s", Utils.stackTraceToString(e)));
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
            logger.info(String.format("%s triggered. UID: %s", apiMethod, uid));

            GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
            request.setParam("UID", uid);
            request.setParam("include", "emails,profile,data,password,userInfo,regSource,identities");
            request.setParam("extraProfileFields", "username,locale,work");
            return request.send();
        } catch (Exception e) {
            logger.error(String.format("An error occurred while retrieving an account. UID: %s. Error: %s", uid, Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse getAccountV2(String uid) {
        try {
            String apiMethod = APIMethods.GET.getValue();
            logger.info(String.format("%s triggered. UID: %s", apiMethod, uid));

            GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
            request.setParam("UID", uid);
            request.setParam("include", "emails,profile,data,password,userInfo,preferences,regSource,identities");
            request.setParam("extraProfileFields", "username,locale,work");
            return request.send();
        } catch (Exception e) {
            logger.error(String.format("An error occurred while retrieving an account. UID: %s. Error: %s", uid, Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse getJWTPublicKey() {
        String apiMethod = APIMethods.GET_JWT_PUBLIC_KEY.getValue();
        logger.info(String.format("%s triggered.", apiMethod));

        GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
        request.setParam("mainApiKey", mainApiKey);
        return request.send();
    }

    /**
     * Updates an account's data in CDC.
     * 
     * @param params object containing data to update.
     * 
     * @return {@code GSResponse} that holds response and error codes from CDC.
     * 
     * @see <a href="https://help.sap.com/viewer/8b8d6fffe113457094a17701f63e3d6a/LATEST/en-US/41398a8670b21014bbc5a10ce4041860.html">accounts.setAccountInfo</a>
     */
    public GSResponse setAccountInfo(GSObject params) {
        String apiMethod = APIMethods.SET_ACCOUNT_INFO.getValue();
        GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
        request.setParams(params);
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

        logger.info(String.format("%s triggered. UID: %s", apiMethod, account.getUID()));
        return request.send();
    }

    public GSResponse setUserInfo(String uid, String data, String profile, String removeLoginEmails, String username) {
        String apiMethod = APIMethods.SET_ACCOUNT_INFO.getValue();
        logger.info(String.format("%s triggered. UID: %s", apiMethod, uid));

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
            logger.info(String.format("%s triggered. UID: %s", apiMethod, uid));

            GSRequest request = GSRequestFactory.create(mainApiKey,mainCdcSecretKey, mainApiDomain, apiMethod);
            request.setParam("UID", uid);
            request.setParam("isActive", status);
            return request.send();
        } catch (Exception e) {
            logger.error(String.format("An error occurred while changing the account status. UID: %s. Error: %s", uid, Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse registerLiteAccount(String email) throws GSKeyNotFoundException, CustomGigyaErrorException {
        final boolean isLite = true;
        GSResponse initRegResponse = initRegistration(isLite);        
        if (CDCUtils.isErrorResponse(initRegResponse)) {
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
        logger.info(String.format("%s triggered. Query: %s", apiMethod, query));

        GSRequest request = buildGSRequest(apiMethod, apiDomain);
        request.setParam("accountTypes", accountType.getValue());
        request.setParam("query", query);
        return request.send();
    }

    public GSResponse register(CDCNewAccount newAccount) {
        try {
            String apiMethod = APIMethods.REGISTER.getValue();
            logger.info(String.format("%s triggered. Username: %s", apiMethod, newAccount.getUsername()));

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
            logger.error(String.format("An error occurred while creating account. Username: %s. Error: %s", newAccount.getUsername(), Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse register(CDCNewAccountV2 newAccount) {
        try {
            String apiMethod = APIMethods.REGISTER.getValue();
            logger.info(String.format("%s triggered. Username: %s", apiMethod, newAccount.getUsername()));

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
            logger.error(String.format("An error occurred while creating account. Username: %s. Error: %s", newAccount.getUsername(), Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse sendVerificationEmail(String uid) {
        try {
            String apiMethod = APIMethods.SEND_VERIFICATION_EMAIL.getValue();
            logger.info(String.format("%s triggered. UID: %s", apiMethod, uid));

            GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
            request.setParam("UID", uid);

            return request.send();
        } catch (Exception e) {
            logger.error(String.format("An error occurred while sending the verification email to the user. UID: %s. Error: %s", uid, Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse resetPassword(GSObject params) {
        try {
            String apiMethod = APIMethods.RESET_PASSWORD.getValue();
            logger.info(String.format("%s triggered.", apiMethod));
            GSRequest request = GSRequestFactory.createWithParams(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod, params);
            return request.send();
        } catch (Exception e) {
            logger.error(String.format("An error occurred while calling the reset password endpoint . Error: %s",  Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse isAvailableLoginId(String loginId, String apiDomain) {
        String apiMethod = APIMethods.IS_AVAILABLE_LOGINID.getValue();
        GSRequest request = buildGSRequest(apiMethod, apiDomain);
        request.setParam("loginID", loginId);
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
        logger.info(String.format("%s triggered. UID: %s", apiMethod, uid));

        GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
        request.setParam("UID", uid);
        request.setParam("data", "{\"requirePasswordCheck\": false}");
        return request.send();
    }

    private GSRequest buildGSRequest(String apiMethod, String apiDomain) {
        GSRequest request;
        if (isMainApiDomain(apiDomain)) {
            logger.info("CDC request built for main domain");
            request =  GSRequestFactory.create(mainApiKey, mainCdcSecretKey, apiDomain, apiMethod);
        } else {
            logger.info("CDC request built for secondary domain");
            request =  GSRequestFactory.create(secondaryApiKey, secondaryDCSecretKey, apiDomain, apiMethod);
        }
        return request;
    }

    private boolean isMainApiDomain(String apiDomain) {
        return apiDomain == null || apiDomain == mainApiDomain;
    }

    private GSResponse initRegistration(boolean isLite) {
        String apiMethod = APIMethods.INIT_REGISTRATION.getValue();
        logger.info(String.format("%s triggered. Lite: %s", apiMethod, Boolean.toString(isLite)));

        GSRequest request = GSRequestFactory.create(mainApiKey, mainCdcSecretKey, mainApiDomain, apiMethod);
        request.setParam("isLite", isLite);

        return request.send();
    }

    private String getRegToken(GSObject data) throws GSKeyNotFoundException {
        return data.getString("regToken");
    }
}
