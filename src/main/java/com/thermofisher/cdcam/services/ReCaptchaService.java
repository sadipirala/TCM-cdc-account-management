package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.model.HttpServiceResponse;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaLowScoreException;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaUnsuccessfulResponseException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ReCaptchaService {

    @Value("${recaptcha.siteverify.url}")
    private String siteVerifyUrl;

    @Value("${recaptcha.threshold.minimum}")
    private double RECAPTCHA_MIN_THRESHOLD;

    @Autowired
    SecretsManager secretsManager;

    @Autowired
    HttpService httpService;

    public JSONObject verifyToken(String reCaptchaToken, String reCaptchaSecret) throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        final String CAPTCHA_SECRET_PROPERTY = "secret-key";

        JSONObject secretProperties = new JSONObject(secretsManager.getSecret(reCaptchaSecret));
        String captchaSecretKeyValue = secretsManager.getProperty(secretProperties, CAPTCHA_SECRET_PROPERTY);

        String url = String.format("%s?secret=%s&response=%s", siteVerifyUrl, captchaSecretKeyValue, reCaptchaToken);
        HttpServiceResponse response = httpService.post(url);

        JSONObject reCaptchaResponse = response.getResponseBody();

        if (reCaptchaResponse.has("score") && !isReCaptchaV3ResponseValid(reCaptchaResponse)) {
            throw new ReCaptchaLowScoreException(reCaptchaResponse.toString());
        } else if (!isReCaptchaResponseValid(reCaptchaResponse)) {
            throw new ReCaptchaUnsuccessfulResponseException(reCaptchaResponse.toString());
        }

        return reCaptchaResponse;
    }

    private boolean isReCaptchaResponseValid(JSONObject reCaptchaResponse) throws JSONException {
        final String SUCCESS = "success";
        return reCaptchaResponse.has(SUCCESS) && reCaptchaResponse.getBoolean(SUCCESS);
    }

    private boolean isReCaptchaV3ResponseValid(JSONObject reCaptchaResponse) throws JSONException {
        return isReCaptchaResponseValid(reCaptchaResponse) && reCaptchaResponse.has("score") && reCaptchaResponse.getDouble("score") >= RECAPTCHA_MIN_THRESHOLD;
    }
}
