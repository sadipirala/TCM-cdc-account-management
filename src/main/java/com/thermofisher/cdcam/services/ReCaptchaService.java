package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.model.HttpServiceResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

    public boolean isTokenValid(String captchaToken) {
        try {
            final String CAPTCHA_SECRET_PROPERTY = "secret-key";
            final String SUCCESS ="success";
            JSONObject EMPTY_BODY = new JSONObject("{}");

            JSONObject secretProperties = new JSONObject(secretsManager.getSecret(capcthaSecret));
            String captchaSecretKeyValue = secretsManager.getProperty(secretProperties, CAPTCHA_SECRET_PROPERTY);

            String url = String.format("%s?secret=%s&response=%s", siteVerifyUrl, captchaSecretKeyValue, captchaToken);
            HttpServiceResponse response = httpService.post(url, EMPTY_BODY);

            JSONObject responseBody = response.getResponseBody();

            return Boolean.parseBoolean(responseBody.getString(SUCCESS));
        }
        catch (Exception ex)
        {
            logger.error(ex.getMessage());
        }
        return true;
    }
}
