package com.thermofisher.cdcam.cdc;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.annotation.PostConstruct;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.enums.cdc.APIMethods;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CDCAccounts {

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
    public void setCredentials() throws ParseException {
        if (env.equals("local") || env.equals("test")) return;
        JSONObject secretProperties = (JSONObject) new JSONParser().parse(secretsManager.getSecret(cdcKey));
        secretKey = secretsManager.getProperty(secretProperties, "secretKey");
        userKey = secretsManager.getProperty(secretProperties, "userKey");
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
            logStackTrace(e);
            return null;
        }
    }

    public GSResponse setUserInfo(String uid, String data, String profile) {
        try {
            String apiMethod = APIMethods.SETINFO.getValue();
            GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);
            if (uid != null) request.setParam("UID", uid);
            if (data != null) request.setParam("data", data);
            if (profile != null) request.setParam("profile", profile);
            return request.send();
        }catch (Exception e) {
            logStackTrace(e);
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
            logStackTrace(e);
            return null;
        }
    }

    public GSResponse search(String query, String accountTypes) {
        if(query == null) return null;
        final boolean USE_HTTPS = true;
        String apiMethod = APIMethods.SEARCH.getValue();
        GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, USE_HTTPS, userKey);
        request.setParam("accountTypes", accountTypes);
        request.setParam("query", query);

        return request.send();
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
            logStackTrace(e);
            return null;
        }
    }

    private void logStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        logger.error(stackTrace);
    }
}
