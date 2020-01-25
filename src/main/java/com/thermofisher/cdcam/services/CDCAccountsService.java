package com.thermofisher.cdcam.services;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.enums.cdc.APIMethods;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import com.thermofisher.cdcam.utils.Utils;

@Service
public class CDCAccountsService {

    final static Logger logger = LogManager.getLogger("CdcamApp");

    @Value("${cdc.apiKey}")
    private String apiKey;

    @Value("${cdc.credentials}")
    private String cdcKey;

    @Value("${env.name}")
    private String env;

    private String userKey;
    private String secretKey;

    @Autowired
    SecretsManager secretsManager;


    @PostConstruct
    public void setCredentials() throws JSONException {
        try {
            if (env.equals("local") || env.equals("test")) return;
            JSONObject secretProperties = new JSONObject(secretsManager.getSecret(cdcKey));
            secretKey = secretsManager.getProperty(secretProperties, "secretKey");
            userKey = secretsManager.getProperty(secretProperties, "userKey");
        } catch (Exception e) {
            Utils.logStackTrace(e, logger);
        }
    }

    public GSResponse getAccount(String UID) {
        try {
            String apiMethod = APIMethods.GET.getValue();
            GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);
            request.setParam("UID", UID);
            request.setParam("include", "emails, profile, data, password, userInfo, regSource, identities");
            request.setParam("extraProfileFields", "username, locale, work");
            return request.send();
        } catch (Exception e) {
            Utils.logStackTrace(e, logger);
            return null;
        }
    }

    public GSResponse setUserInfo(String uid, String data, String profile) {
        try {
            String apiMethod = APIMethods.SETINFO.getValue();
            GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);
            request.setParam("UID", uid);
            request.setParam("data", data);
            request.setParam("profile", profile);
            return request.send();
        } catch (Exception e) {
            Utils.logStackTrace(e, logger);
            return null;
        }
    }

    public GSResponse setLiteReg(String email) {
        try {
            String apiMethod = APIMethods.SETINFO.getValue();
            GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);
            request.setParam("regToken", getRegToken(true));
            request.setParam("profile", String.format("{\"email\":\"%s\"}", email));
            return request.send();
        } catch (Exception e) {
            Utils.logStackTrace(e, logger);
            return null;
        }
    }

    public GSResponse search(String query, String accountTypes) {
        if (query == null) return null;
        final boolean USE_HTTPS = true;
        String apiMethod = APIMethods.SEARCH.getValue();
        GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, USE_HTTPS, userKey);
        request.setParam("accountTypes", accountTypes);
        request.setParam("query", query);
        return request.send();
    }

    public GSResponse register(String username, String email, String password, String data, String profile) {
        try {
            String apiMethod = APIMethods.REGISTER.getValue();
            GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);
            request.setParam("username", username);
            request.setParam("email", email);
            request.setParam("password", password);
            request.setParam("data", data);
            request.setParam("profile", profile);
            return request.send();
        } catch (Exception ex) {
            Utils.logStackTrace(ex, logger);
            return null;
        }
    }

    private String getRegToken(boolean isLite) {
        try {
            String apiMethod = APIMethods.INITREG.getValue();
            GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);
            request.setParam("isLite", isLite);

            GSResponse response = request.send();
            if (response.getErrorCode() == 0) {
                GSObject obj = response.getData();
                return obj.getString("regToken");
            } else {
                return ("Uh-oh, we got the following error: " + response.getLog());
            }
        } catch (Exception e) {
            Utils.logStackTrace(e, logger);
            return null;
        }
    }


}
