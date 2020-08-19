package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.enums.CaptchaErrors;
import com.thermofisher.cdcam.model.HttpServiceResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class ReCaptchaService {
    private Logger logger = LogManager.getLogger(this.getClass());

    @Value("${recaptcha.siteverify.url}")
    private String siteVerifyUrl;

    @Value("${recaptcha.secret.key}")
    private String capcthaSecret;

    @Autowired
    SecretsManager secretsManager;

    @Autowired
    HttpService httpService;

    public JSONObject verifyToken(String captchaToken) throws JSONException {
        try {
            final String CAPTCHA_SECRET_PROPERTY = "secret-key";

            JSONObject secretProperties = new JSONObject(secretsManager.getSecret(capcthaSecret));
            String captchaSecretKeyValue = secretsManager.getProperty(secretProperties, CAPTCHA_SECRET_PROPERTY);

            String url = String.format("%s?secret=%s&response=%s", siteVerifyUrl, captchaSecretKeyValue, captchaToken);
            HttpServiceResponse response = httpService.post(url);

            JSONObject responseBody = response.getResponseBody();

            return responseBody;
        }
        catch (Exception ex)
        {
            logger.error(ex.getMessage());
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("success",false);
            errorResponse.put("error-codes",new String[]{CaptchaErrors.VERIFY_TOKEN_EXCEPTION.getValue()});
            return errorResponse;
        }
    }
}
