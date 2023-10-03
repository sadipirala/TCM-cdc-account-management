package com.thermofisher.cdcam.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.model.HttpServiceResponse;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaLowScoreException;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaUnsuccessfulResponseException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
@SpringBootTest//(classes = CdcamApplication.class)
public class ReCaptchaServiceTests {
    private final String reCaptchaToken = "";
    private final String captchaValidationJWT = "";

    @InjectMocks
    ReCaptchaService reCaptchaService;

    @Mock
    HttpService httpService;

    @Mock
    JWTService jwtService;

    @Mock
    SecretsService secretsService;


    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(reCaptchaService, "RECAPTCHA_MIN_THRESHOLD", 0.5);
    }

    @Test
    public void verifyToken_givenReCaptchaTokenIsValidAndScoreIsEqualsToMinThreshold_ThenRecaptchaResponseShouldBeReturned()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        JSONObject reCaptchaResponse = new JSONObject();
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.5);
        HttpServiceResponse httpResponse = HttpServiceResponse.builder().responseBody(reCaptchaResponse).build();
        when(httpService.post(any())).thenReturn(httpResponse);

        // then
        JSONObject response = reCaptchaService.verifyToken(reCaptchaToken, captchaValidationJWT);

        // then
        assertTrue(reCaptchaResponse.equals(response));
    }

    @Test(expected = ReCaptchaLowScoreException.class)
    public void verifyToken_givenReCaptchaTokenIsValidAndScoreIsLowerThanMinThreshold_ThenReCaptchaLowScoreExceptionShouldBeThrown()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        JSONObject reCaptchaResponse = new JSONObject();
        reCaptchaResponse.put("success", true);
        reCaptchaResponse.put("score", 0.4);
        HttpServiceResponse httpResponse = HttpServiceResponse.builder().responseBody(reCaptchaResponse).build();
        when(httpService.post(any())).thenReturn(httpResponse);

        // then
        reCaptchaService.verifyToken(reCaptchaToken, captchaValidationJWT);
    }

    @Test(expected = ReCaptchaUnsuccessfulResponseException.class)
    public void verifyToken_givenReCaptchaTokenIsValidAndHasNoScoreAndIsUnsuccessful_ThenReCaptchaUnsuccessfulResponseExceptionShouldBeThrown()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        JSONObject reCaptchaResponse = new JSONObject();
        reCaptchaResponse.put("success", false);
        HttpServiceResponse httpResponse = HttpServiceResponse.builder().responseBody(reCaptchaResponse).build();
        when(httpService.post(any())).thenReturn(httpResponse);

        // then
        reCaptchaService.verifyToken(reCaptchaToken, captchaValidationJWT);
    }
}
