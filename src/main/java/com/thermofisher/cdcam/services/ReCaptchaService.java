package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.enums.aws.CdcamSecrets;
import com.thermofisher.cdcam.model.HttpServiceResponse;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaLowScoreException;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaUnsuccessfulResponseException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReCaptchaService {
    public static final String CAPTCHA_TOKEN_HEADER = "x-captcha-token";
    private String reCaptchaV3Secret;
    private String reCaptchaV2Secret;

    @Value("${env.name}")
    private String env;

    @Value("${recaptcha.siteverify.url}")
    private String siteVerifyUrl;

    @Value("${recaptcha.threshold.minimum}")
    private double RECAPTCHA_MIN_THRESHOLD;

    @Autowired
    JWTService jwtService;

    @Autowired
    SecretsService secretsService;

    @PostConstruct
    public void setup() throws JSONException {
        if (env.equals("local") || env.equals("test")) return;
        reCaptchaV3Secret = secretsService.get(CdcamSecrets.RECAPTCHAV3.getKey());
        reCaptchaV2Secret = secretsService.get(CdcamSecrets.RECAPTCHAV2.getKey());
    }

    @Autowired
    HttpService httpService;

    public JSONObject verifyToken(String reCaptchaToken, String captchaValidationJWT) throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        String reCaptchaSecret = this.getReCaptchaSecret(captchaValidationJWT);
        String url = String.format("%s?secret=%s&response=%s", siteVerifyUrl, reCaptchaSecret, reCaptchaToken);
        HttpServiceResponse response = httpService.post(url);
        JSONObject reCaptchaResponse = response.getResponseBody();

        if (reCaptchaResponse.has("score") && !isReCaptchaV3ResponseValid(reCaptchaResponse)) {
            throw new ReCaptchaLowScoreException(reCaptchaResponse.toString());
        } else if (!isReCaptchaResponseValid(reCaptchaResponse)) {
            throw new ReCaptchaUnsuccessfulResponseException(reCaptchaResponse.toString());
        }

        return reCaptchaResponse;
    }

    private String getReCaptchaSecret(String captchaValidationJWT) {
        if (StringUtils.isBlank(captchaValidationJWT)) {
            log.info("No captcha token, using reCaptcha v3.");
            return reCaptchaV3Secret;
        } else {
            log.info("Captcha validation token received, verifying JWT: {}", captchaValidationJWT);
            jwtService.verify(captchaValidationJWT);
            log.info("Valid captcha JWT, using reCaptchaV2.");
            return reCaptchaV2Secret;
        }
    }

    private boolean isReCaptchaResponseValid(JSONObject reCaptchaResponse) throws JSONException {
        final String SUCCESS = "success";
        return reCaptchaResponse.has(SUCCESS) && reCaptchaResponse.getBoolean(SUCCESS);
    }

    private boolean isReCaptchaV3ResponseValid(JSONObject reCaptchaResponse) throws JSONException {
        return isReCaptchaResponseValid(reCaptchaResponse) && reCaptchaResponse.has("score") && reCaptchaResponse.getDouble("score") >= RECAPTCHA_MIN_THRESHOLD;
    }
}
