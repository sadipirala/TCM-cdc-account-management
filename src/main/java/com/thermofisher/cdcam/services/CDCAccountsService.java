package com.thermofisher.cdcam.services;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.enums.cdc.APIMethods;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;

import com.thermofisher.cdcam.model.ResetPassword;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import com.thermofisher.cdcam.utils.Utils;

@Service
public class CDCAccountsService {

    private Logger logger = LogManager.getLogger(this.getClass());

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
    public void setCredentials() {
        try {
            if (env.equals("local") || env.equals("test"))
                return;
            logger.info("Setting up CDC credentials.");
            JSONObject secretProperties = new JSONObject(secretsManager.getSecret(cdcKey));
            secretKey = secretsManager.getProperty(secretProperties, "secretKey");
            userKey = secretsManager.getProperty(secretProperties, "userKey");
        } catch (Exception e) {
            logger.error(String.format("An error occurred while configuring CDC credentials. Error: %s",
                    Utils.stackTraceToString(e)));
        }
    }

    public GSResponse getAccount(String uid) {
        try {
            String apiMethod = APIMethods.GET.getValue();
            logger.info(String.format("%s triggered. UID: %s", apiMethod, uid));

            GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);
            request.setParam("UID", uid);
            request.setParam("include", "emails, profile, data, password, userInfo, regSource, identities");
            request.setParam("extraProfileFields", "username, locale, work");
            return request.send();
        } catch (Exception e) {
            logger.error(String.format("An error occurred while retrieving an account. UID: %s. Error: %s", uid,
                    Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse setUserInfo(String uid, String data, String profile) {
        try {
            String apiMethod = APIMethods.SETINFO.getValue();
            logger.info(String.format("%s triggered. UID: %s", apiMethod, uid));

            GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);
            request.setParam("UID", uid);
            request.setParam("data", data);
            request.setParam("profile", profile);
            return request.send();
        } catch (Exception e) {
            logger.error(String.format("An error occurred while updating an account. UID: %s. Error: %s", uid,
                    Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse changeAccountStatus(String uid, boolean status) {
        try {
            String apiMethod = APIMethods.SETINFO.getValue();
            logger.info(String.format("%s triggered. UID: %s", apiMethod, uid));

            GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);
            request.setParam("UID", uid);
            request.setParam("isActive", status);
            return request.send();
        } catch (Exception e) {
            logger.error(String.format("An error occurred while changing the account status. UID: %s. Error: %s", uid,
                    Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse setLiteReg(String email) {
        try {
            String apiMethod = APIMethods.SETINFO.getValue();
            logger.info(String.format("%s (email-only registration) triggered. Email: %s", apiMethod, email));

            GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);
            request.setParam("regToken", getRegToken(true));
            request.setParam("profile", String.format("{\"email\":\"%s\"}", email));
            return request.send();
        } catch (Exception e) {
            logger.error(String.format("An error occurred while creating email only account. Email: %s. Error: %s",
                    email, Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse search(String query, String accountTypes) {
        final boolean USE_HTTPS = true;

        String apiMethod = APIMethods.SEARCH.getValue();
        logger.info(String.format("%s triggered. Query: %s", apiMethod, query));

        if (query == null)
            return null;

        GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, USE_HTTPS, userKey);
        request.setParam("accountTypes", accountTypes);
        request.setParam("query", query);
        return request.send();
    }

    public GSResponse register(CDCNewAccount newAccount) {
        try {
            String apiMethod = APIMethods.REGISTER.getValue();
            logger.info(String.format("%s triggered. Username: %s", apiMethod, newAccount.getUsername()));

            GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);
            request.setParam("username", newAccount.getUsername());
            request.setParam("email", newAccount.getEmail());
            request.setParam("password", newAccount.getPassword());
            request.setParam("data", newAccount.getData());
            request.setParam("profile", newAccount.getProfile());
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

            GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);
            request.setParam("UID", uid);

            return request.send();
        } catch (Exception e) {
            logger.error(String.format("An error occurred while sending the verification email to the user. UID: %s. Error: %s", uid, Utils.stackTraceToString(e)));
            return null;
        }
    }

    public GSResponse resetPasswordRequest(ResetPassword resetPassword) {
        try {
            String apiMethod = APIMethods.RESET_PASSWORD.getValue();
            logger.info(String.format("%s triggered. value: %s", apiMethod, (resetPassword.getUsername() == null || resetPassword.getUsername().isEmpty()) ? resetPassword.getResetPasswordToken() :resetPassword.getUsername()));

            GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);

            if(resetPassword.getResetPasswordToken() == null || resetPassword.getResetPasswordToken().isEmpty())
                request.setParam("loginID", resetPassword.getUsername());
            else {
                request.setParam("passwordResetToken",resetPassword.getResetPasswordToken());
                request.setParam("newPassword",resetPassword.getNewPassword());
            }

            return request.send();
        } catch (Exception e) {
            logger.error(String.format("An error occurred while calling the reset password endpoint . Error: %s",  Utils.stackTraceToString(e)));
            return null;
        }
    }

    private String getRegToken(boolean isLite) {
        try {
            String apiMethod = APIMethods.INITREG.getValue();
            logger.info(String.format("%s triggered. Email-only: %s", apiMethod, Boolean.toString(isLite)));

            GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);
            request.setParam("isLite", isLite);

            GSResponse response = request.send();
            if (response.getErrorCode() == 0) {
                GSObject obj = response.getData();
                return obj.getString("regToken");
            } else {
                String message = String.format("An error occurred while generating a regToken. Error: %s", response.getErrorMessage());
                logger.error(message);

                return (message);
            }
        } catch (Exception e) {
            logger.error(String.format("An error occurred while generating a regToken. Error: %s", Utils.stackTraceToString(e)));
            return null;
        }
    }


}
