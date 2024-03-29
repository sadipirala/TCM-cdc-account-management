package com.thermofisher.cdcam;

import com.gigya.socialize.GSKeyNotFoundException;
import com.thermofisher.cdcam.controller.ResetPasswordController;
import com.thermofisher.cdcam.model.ResetPasswordRequest;
import com.thermofisher.cdcam.model.ResetPasswordResponse;
import com.thermofisher.cdcam.model.ResetPasswordSubmit;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.LoginIdDoesNotExistException;
import com.thermofisher.cdcam.model.cdc.OpenIdRelyingParty;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaLowScoreException;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaUnsuccessfulResponseException;
import com.thermofisher.cdcam.services.CookieService;
import com.thermofisher.cdcam.services.EncodeService;
import com.thermofisher.cdcam.services.GigyaService;
import com.thermofisher.cdcam.services.JWTService;
import com.thermofisher.cdcam.services.LoginService;
import com.thermofisher.cdcam.services.NotificationService;
import com.thermofisher.cdcam.services.ReCaptchaService;
import com.thermofisher.cdcam.services.SecretsService;
import com.thermofisher.cdcam.utils.Utils;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResetPasswordControllerTests {
    String username = "armadillo@mail.com";
    String email = "armadillo@mail.com";
    JSONObject reCaptchaResponse;
    ResetPasswordRequest resetPasswordRequestBody;
    ResetPasswordResponse resetPasswordResponseMock;
    private String CLIENT_ID = "1000000";
    private String REDIRECT_URL = "http://example.com";
    private String STATE = "state";
    private String RESPONSE_TYPE = "responseType";
    private String SCOPE = "scope";
    private String COOKIE_CIP_AUTHDATA_VALID = "eyJjbGllbnRJZCI6ImNsaWVudElkIiwicmVkaXJlY3RVcmkiOiJyZWRpcmVjdFVyaSIsInN0YXRlIjoic3RhdGUiLCJzY29wZSI6InNjb3BlIiwicmVzcG9uc2VUeXBlIjoicmVzcG9uc2VUeXBlIn0=";

    @InjectMocks
    ResetPasswordController resetPasswordController;

    @Mock
    GigyaService gigyaService;

    @Mock
    CookieService cookieService;

    @Mock
    EncodeService encodeService;

    @Mock
    LoginService loginService;

    @Mock
    JWTService jwtService;

    @Mock
    NotificationService notificationService;

    @Mock
    ReCaptchaService reCaptchaService;

    @Mock
    SecretsService secretsService;


    @Captor
    ArgumentCaptor<String> reCaptchaSecretCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        reCaptchaResponse = new JSONObject();
        resetPasswordRequestBody = mock(ResetPasswordRequest.class);
        resetPasswordResponseMock = mock(ResetPasswordResponse.class);
    }

    private void setSendResetPasswordEmailMocks() {
        when(resetPasswordRequestBody.getUsername()).thenReturn(username);
        when(resetPasswordRequestBody.getCaptchaToken()).thenReturn("token");
    }

    @Test
    public void sendResetPasswordEmail_ShouldVerifyTheReCaptchaToken()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        String captchaValidationToken = RandomStringUtils.random(10);
        when(reCaptchaService.verifyToken(any(), eq(captchaValidationToken))).thenReturn(reCaptchaResponse);
        setSendResetPasswordEmailMocks();

        // when
        resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody, COOKIE_CIP_AUTHDATA_VALID, captchaValidationToken);

        // then
        verify(reCaptchaService).verifyToken(anyString(), eq(captchaValidationToken));
    }

    @Test
    public void sendResetPasswordEmail_WhenReCaptchaTokenVerificationThrowsReCaptchaLowScoreException_returnAccepted() throws JSONException,
            ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        when(reCaptchaService.verifyToken(any(), any())).thenThrow(new ReCaptchaLowScoreException(""));
        when(jwtService.create()).thenReturn(RandomStringUtils.random(10));
        setSendResetPasswordEmailMocks();

        // when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        assertEquals(result.getStatusCode().value(), HttpStatus.ACCEPTED.value());
        assertNotNull(result.getHeaders().get(ReCaptchaService.CAPTCHA_TOKEN_HEADER));
        verify(jwtService).create();
    }

    @Test
    public void sendResetPasswordEmail_WhenReCaptchaTokenVerificationThrowsReCaptchaUnsuccessfulResponseException_returnBadRequest() throws JSONException,
            ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        when(reCaptchaService.verifyToken(any(), any())).thenThrow(new ReCaptchaUnsuccessfulResponseException(""));
        setSendResetPasswordEmailMocks();

        // when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        assertEquals(result.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void sendResetPasswordEmail_WhenReCaptchaVerificationIsSuccessful_returnOk() throws JSONException,
            ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        String redirectUrl = "http://test.com";
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
//        when(loginService.generateDefaultLoginUrl(redirectUrl)).thenReturn("");
        setSendResetPasswordEmailMocks();

        // when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        assertEquals(result.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void sendResetPasswordEmail_WhenATokenIsValidAndTheAccountDoesNotExistsInCDC_returnOk()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        when(resetPasswordRequestBody.getCaptchaToken()).thenReturn("token");
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody, COOKIE_CIP_AUTHDATA_VALID, null);

        // then
        assertEquals(result.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void sendResetPasswordEmail_WhenATokenIsValidAndTheAccountExistInCDC_returnOK()
            throws JSONException, IOException, CustomGigyaErrorException, LoginIdDoesNotExistException,
            ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, GSKeyNotFoundException {
        //given
        setSendResetPasswordEmailMocks();
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(gigyaService.getEmailByUsername(username)).thenReturn(email);
        when(gigyaService.resetPasswordRequest(username)).thenReturn("");

        //when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody, COOKIE_CIP_AUTHDATA_VALID, null);

        //then
        assertEquals(result.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void sendResetPasswordEmail_WhenCIPAuthDataDoesntExists_ThenADefaultCookieShouldBeBuildAndReturnOK()
            throws JSONException, IOException, CustomGigyaErrorException, LoginIdDoesNotExistException,
            ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, GSKeyNotFoundException {
        //given
        setSendResetPasswordEmailMocks();
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(gigyaService.getEmailByUsername(username)).thenReturn(email);
        when(gigyaService.resetPasswordRequest(username)).thenReturn("");
//        when(encodeService.encodeBase64(anyString())).thenReturn(COOKIE_CIP_AUTHDATA_VALID.getBytes());

        //when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody, new String(), null);

        //then
        assertEquals(result.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void sendResetPasswordEmail_WhenLoginIdDoesNotExistExceptionIsThrown_ThenReturnHttpStatusOK()
            throws JSONException, IOException, CustomGigyaErrorException, LoginIdDoesNotExistException,
            ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, GSKeyNotFoundException {
        //given
        setSendResetPasswordEmailMocks();
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
//        when(gigyaService.getEmailByUsername(username)).thenReturn(email);
        when(gigyaService.resetPasswordRequest(username)).thenThrow(new LoginIdDoesNotExistException(""));
//        when(encodeService.encodeBase64(anyString())).thenReturn(COOKIE_CIP_AUTHDATA_VALID.getBytes());

        //when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody, new String(), null);

        //then
        assertEquals(result.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void sendResetPasswordEmail_WhenCustomGigyaErrorExceptionIsThrown_ThenReturnHttpStatusBadRequest()
            throws JSONException, IOException, CustomGigyaErrorException, LoginIdDoesNotExistException,
            ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, GSKeyNotFoundException {
        //given
        setSendResetPasswordEmailMocks();
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
//        when(gigyaService.getEmailByUsername(username)).thenReturn(email);
        when(gigyaService.resetPasswordRequest(username)).thenThrow(new CustomGigyaErrorException(""));
        //       when(encodeService.encodeBase64(anyString())).thenReturn(COOKIE_CIP_AUTHDATA_VALID.getBytes());

        //when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody, new String(), null);

        //then
        assertEquals(result.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void sendResetPasswordEmail_WhenNullPointerExceptionIsThrown_ThenReturnHttpStatusInternalServerError()
            throws JSONException, IOException, CustomGigyaErrorException, LoginIdDoesNotExistException,
            ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, GSKeyNotFoundException {
        //given
        setSendResetPasswordEmailMocks();
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
//        when(gigyaService.getEmailByUsername(username)).thenReturn(email);
        when(gigyaService.resetPasswordRequest(username)).thenThrow(new NullPointerException(""));
//        when(encodeService.encodeBase64(anyString())).thenReturn(COOKIE_CIP_AUTHDATA_VALID.getBytes());

        //when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody, new String(), null);

        //then
        assertEquals(result.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void resetPassword_WhenAValidBodyIsSent_ShouldTriggerRequestForResetPasswordConfirmationEmail_AndReturnOK() throws CustomGigyaErrorException {
        //given
        ResetPasswordSubmit mockResetPasswordBody = ResetPasswordSubmit.builder()
                .newPassword("testPassword1")
                .resetPasswordToken("testTkn")
                .uid("62623d97356b4815a9965d912fa3331a")
                .build();
        when(resetPasswordResponseMock.getResponseCode()).thenReturn(0);
        when(gigyaService.resetPasswordSubmit(mockResetPasswordBody)).thenReturn(resetPasswordResponseMock);
//        when(gigyaService.getAccountInfo(any())).thenReturn(AccountUtils.getSiteAccount());
        doNothing().when(notificationService).sendPasswordUpdateNotification(any());

        //when
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = resetPasswordController.resetPassword(mockResetPasswordBody);

        //then
        assertEquals(resetPasswordResponse.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void resetPassword_WhenTheSNSHandlerFails_returnINTERNAL_SERVER_ERROR() {
        // given
        ResetPasswordSubmit mockResetPasswordBody = ResetPasswordSubmit.builder()
                .newPassword("testPassword1")
                .resetPasswordToken("testTkn")
                .uid("62623d97356b4815a9965d912fa3331a").build();
        when(resetPasswordResponseMock.getResponseCode()).thenReturn(0);
        when(gigyaService.resetPasswordSubmit(mockResetPasswordBody)).thenReturn(resetPasswordResponseMock);
        doThrow(NullPointerException.class).when(notificationService).sendPasswordUpdateNotification(any());

        // when
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = resetPasswordController.resetPassword(mockResetPasswordBody);

        // then
        assertEquals(resetPasswordResponse.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void resetPassword_WhenAnExceptionIsThrown_returnINTERNAL_SERVER_ERROR() {
        //given
        ResetPasswordSubmit mockResetPasswordBody = ResetPasswordSubmit.builder()
                .newPassword("testPassword1")
                .resetPasswordToken("testTkn")
                .uid("62623d97356b4815a9965d912fa3331a").build();
        when(resetPasswordResponseMock.getResponseCode()).thenReturn(0);
        when(gigyaService.resetPasswordSubmit(any())).thenReturn(resetPasswordResponseMock);
        doThrow(NullPointerException.class).when(notificationService).sendPasswordUpdateNotification(any());

        //when
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = resetPasswordController.resetPassword(mockResetPasswordBody);

        //then
        assertEquals(resetPasswordResponse.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void resetPassword_WhenTheResetPasswordOnCDCFails_returnBAD_REQUEST() {
        //given
        ResetPasswordSubmit mockResetPasswordBody = ResetPasswordSubmit.builder()
                .newPassword("testPassword1")
                .resetPasswordToken("testTkn")
                .uid("62623d97356b4815a9965d912fa3331a").build();
        when(resetPasswordResponseMock.getResponseCode()).thenReturn(40001);
        when(gigyaService.resetPasswordSubmit(any())).thenReturn(resetPasswordResponseMock);


        //when
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = resetPasswordController.resetPassword(mockResetPasswordBody);

        //then
        assertEquals(resetPasswordResponse.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void resetPassword_WhenTheResetPasswordTokenExpires_returnFOUND() {
        //given
        ResetPasswordSubmit mockResetPasswordBody = ResetPasswordSubmit.builder()
                .newPassword("testPassword1")
                .resetPasswordToken("testTkn")
                .uid("62623d97356b4815a9965d912fa3331a").build();
        when(resetPasswordResponseMock.getResponseCode()).thenReturn(403025);
        when(gigyaService.resetPasswordSubmit(any())).thenReturn(resetPasswordResponseMock);


        //when
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = resetPasswordController.resetPassword(mockResetPasswordBody);

        //then
        assertEquals(resetPasswordResponse.getStatusCode(), HttpStatus.FOUND);
    }

    @Test
    public void getRPResetPasswordConfig_GivenMethodCalled_WhenParametersAreValid_ThenShouldReturnFoundStatusAndCIP_AUTDATAShouldBePresentInHeaders() throws Exception {
        // given
        List<String> redirectUris = new ArrayList<>(Arrays.asList("http://example.com", "http://example2.com"));
        String description = "Description";
        String params = "?state=state&redirect_uri=redirect";
        OpenIdRelyingParty openIdRelyingParty = OpenIdRelyingParty.builder()
                .clientId(CLIENT_ID)
                .description(description)
                .redirectUris(redirectUris)
                .build();
        when(gigyaService.getRP(anyString())).thenReturn(openIdRelyingParty);
//        when(encodeService.encodeUTF8(anyString())).thenReturn(URLDecoder.decode(params, StandardCharsets.UTF_8.toString()));
        when(cookieService.createCIPAuthDataCookie(any(), any())).thenReturn(anyString());
        // when
        ResponseEntity<?> response = resetPasswordController.getRPResetPasswordConfig(CLIENT_ID, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.FOUND);
        assertTrue(!Utils.isNullOrEmpty(response.getHeaders().get("Set-Cookie")));
        assertTrue(!Utils.isNullOrEmpty(response.getHeaders().get("Location")));
    }

    @Test
    public void getRPResetPasswordConfig_GivenMethodCalled_WhenParametersAreValidAndURiDoesNotExistInClientURIs_ThenShouldReturnBadRequest() throws Exception {
        // given
        String redirectUtl = "http://example3.com";
        List<String> redirectUris = new ArrayList<>(Arrays.asList("http://example.com", "http://example2.com"));
        String description = "Description";
        OpenIdRelyingParty openIdRelyingParty = OpenIdRelyingParty.builder()
                .clientId(CLIENT_ID)
                .description(description)
                .redirectUris(redirectUris)
                .build();
        when(gigyaService.getRP(anyString())).thenReturn(openIdRelyingParty);

        // when
        ResponseEntity<?> response = resetPasswordController.getRPResetPasswordConfig(CLIENT_ID, redirectUtl, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getRPResetPasswordConfig_GivenMethodCalled_WhenParametersAreValidAndClientIdDoesNotExists_ThenShouldReturnBadRequest() throws Exception {
        // given
        when(gigyaService.getRP(anyString())).thenThrow(new CustomGigyaErrorException("404000"));

        // when
        ResponseEntity<?> response = resetPasswordController.getRPResetPasswordConfig(CLIENT_ID, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getRPResetPasswordConfig_GivenMethodCalled_WhenParametersAreValidAndAErrorOccurred_ThenShouldReturnBadRequest() throws Exception {
        // given
        when(gigyaService.getRP(anyString())).thenThrow(new CustomGigyaErrorException("599999"));

        // when
        ResponseEntity<?> response = resetPasswordController.getRPResetPasswordConfig(CLIENT_ID, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getRPResetPasswordConfig_GivenMethodCalled_WhenClientIDIsNullOrEmpty_ThenShouldReturnBadRequest() throws Exception {
        // when
        ResponseEntity<?> response = resetPasswordController.getRPResetPasswordConfig(null, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getRPResetPasswordConfig_GivenMethodCalled_WhenRedirectURLIsNullOrEmpty_ThenShouldReturnBadRequest() throws Exception {
        // when
        ResponseEntity<?> response = resetPasswordController.getRPResetPasswordConfig(CLIENT_ID, "", STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getRPResetPasswordConfig_WhenExceptionJSONExceptionIsThrown_ThenReturnHttpStatusInternalServerError() throws Exception {
        when(gigyaService.getRP(anyString())).thenThrow(new GSKeyNotFoundException(""));

        // when
        ResponseEntity<?> response = resetPasswordController.getRPResetPasswordConfig(CLIENT_ID, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void resetPassword_GivenUpdateForRequirePasswordCheck_ResponseShouldBeSuccessful() throws CustomGigyaErrorException {
        // given
        ResetPasswordSubmit mockResetPasswordBody = ResetPasswordSubmit.builder()
                .newPassword("testPassword1")
                .resetPasswordToken("testTkn")
                .uid("62623d97356b4815a9965d912fa3331a").build();
        when(gigyaService.resetPasswordSubmit(mockResetPasswordBody)).thenReturn(resetPasswordResponseMock);
        doNothing().when(gigyaService).updateRequirePasswordCheck(any());

        // when
        ResponseEntity<ResetPasswordResponse> response = resetPasswordController.resetPassword(mockResetPasswordBody);

        // then
        verify(gigyaService).updateRequirePasswordCheck(any());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void resetPassword_GivenUpdateRequirePasswordCheckThrowsException_ResponseShouldStillBeSuccessful() throws CustomGigyaErrorException {
        // given
        ResetPasswordSubmit mockResetPasswordBody = ResetPasswordSubmit.builder()
                .newPassword("testPassword1")
                .resetPasswordToken("testTkn")
                .uid("62623d97356b4815a9965d912fa3331a").build();
        when(gigyaService.resetPasswordSubmit(mockResetPasswordBody)).thenReturn(resetPasswordResponseMock);
        doThrow(CustomGigyaErrorException.class).when(gigyaService).updateRequirePasswordCheck(any());

        // when
        ResponseEntity<ResetPasswordResponse> response = resetPasswordController.resetPassword(mockResetPasswordBody);

        // then
        verify(gigyaService).updateRequirePasswordCheck(any());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }
}
