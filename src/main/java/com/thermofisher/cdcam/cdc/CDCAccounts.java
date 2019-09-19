package com.thermofisher.cdcam.cdc;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.enums.cdc.APIMethods;
import com.thermofisher.cdcam.model.AccountInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.io.PrintWriter;
import java.io.StringWriter;

@Configuration
public class CDCAccounts {

    final static Logger logger = LogManager.getLogger("CdcamApp");

    @Value("${cdc.apiKey}")
    private String apiKey;

    @Value("${cdc.secretKey}")
    private String secretKey;

    @Value("${cdc.userKey}")
    private String userKey;

    @Value("${eec.request.limit}")
    private int requestLimit;

    AccountBuilder accountBuilder = new AccountBuilder();

    public int getLiteRegLimit() {
        return requestLimit;
    }

    public AccountInfo getAccount(String UID) {
        try {
            String apiMethod = APIMethods.GET.getValue();
            GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);
            request.setParam("UID", UID);
            request.setParam("include", "emails, profile, data, password,userInfo,regSource,identities");
            request.setParam("extraProfileFields", "username, locale,work");

            GSResponse response = request.send();
            if (response.getErrorCode() == 0) {
                GSObject obj = response.getData();
                logger.info("User Found: " + obj.get("UID"));
                return accountBuilder.getAccountInfo(obj);
            } else {
                logger.error(response.getErrorDetails());
                return null;
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            logger.error(stackTrace);
            return null;
        }
    }

    public GSResponse setLiteReg(String email) {
        try {
            String apiMethod = APIMethods.SETINFO.getValue();
            GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);
            request.setParam("regToken", getRegToken(true));
            request.setParam("profile", String.format("{\"email\":\"%s\"}", email));
            GSResponse response = request.send();

            return response;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            logger.error(stackTrace);
            return null;
        }
    }

    public GSResponse searchByEmail(String email) {
        if(email == null) return null;
        final boolean USE_HTTPS = true;
        final String ACCOUNT_TYPES = "full,lite";
        final String QUERY_BY_USERNAME_OR_EMAIL = String.format("SELECT * FROM accounts WHERE profile.username = '%1$s' OR profile.email = '%1$s'", email);
        String apiMethod = APIMethods.SEARCH.getValue();

        GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, USE_HTTPS, userKey);
        request.setParam("accountTypes", ACCOUNT_TYPES);
        request.setParam("query", QUERY_BY_USERNAME_OR_EMAIL);

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
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            logger.error(stackTrace);
            return null;
        }
    }
}
