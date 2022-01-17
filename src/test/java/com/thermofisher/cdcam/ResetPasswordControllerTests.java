package com.thermofisher.cdcam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gigya.socialize.GSKeyNotFoundException;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.controller.ResetPasswordController;
import com.thermofisher.cdcam.enums.aws.CdcamSecrets;
import com.thermofisher.cdcam.model.ResetPasswordRequest;
import com.thermofisher.cdcam.model.ResetPasswordResponse;
import com.thermofisher.cdcam.model.ResetPasswordSubmit;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.LoginIdDoesNotExistException;
import com.thermofisher.cdcam.model.cdc.OpenIdRelyingParty;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaLowScoreException;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaUnsuccessfulResponseException;
import com.thermofisher.cdcam.services.*;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
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
    CDCResponseHandler cdcResponseHandler;

    @Mock
    NotificationService notificationService;

    @Mock
    ReCaptchaService reCaptchaService;

    @Mock
    SecretsService secretsService;

    @Mock
    EncodeService encodeService;

    @Mock
    CookieService cookieService;

    @Mock
    IdentityAuthorizationService identityAuthorizationService;
    
    @Captor
    ArgumentCaptor<String> reCaptchaSecretCaptor;

    @Before
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
    public void sendResetPasswordEmail_givenReCaptchaVersionIsV2_ThenReCaptchaServiceShouldGetCalledWithReCaptchaV2Secret()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        String expectedReCaptchaV2Secret = RandomStringUtils.random(10);
        when(secretsService.get(CdcamSecrets.RECAPTCHAV2.getKey())).thenReturn(expectedReCaptchaV2Secret);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(resetPasswordRequestBody.getIsReCaptchaV2()).thenReturn(true);
        setSendResetPasswordEmailMocks();

        // when
        resetPasswordController.sendResetPasswordEmail(COOKIE_CIP_AUTHDATA_VALID,resetPasswordRequestBody);

        // then
        verify(reCaptchaService).verifyToken(anyString(), reCaptchaSecretCaptor.capture());
        String reCaptchaSecret = reCaptchaSecretCaptor.getValue();
        assertEquals(expectedReCaptchaV2Secret, reCaptchaSecret);
    }

    @Test
    public void sendResetPasswordEmail_givenReCaptchaVersionIsV3_ThenReCaptchaServiceShouldGetCalledWithReCaptchaV3Secret()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        String expectedReCaptchaV3Secret = RandomStringUtils.random(10);
        when(secretsService.get(CdcamSecrets.RECAPTCHAV3.getKey())).thenReturn(expectedReCaptchaV3Secret);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        setSendResetPasswordEmailMocks();

        // when
        resetPasswordController.sendResetPasswordEmail(COOKIE_CIP_AUTHDATA_VALID,resetPasswordRequestBody);

        // then
        verify(reCaptchaService).verifyToken(anyString(), reCaptchaSecretCaptor.capture());
        String reCaptchaSecret = reCaptchaSecretCaptor.getValue();
        assertEquals(expectedReCaptchaV3Secret, reCaptchaSecret);
    }

    @Test
    public void sendResetPasswordEmail_WhenReCaptchaTokenVerificationThrowsReCaptchaLowScoreException_returnAccepted() throws JSONException,
            ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        when(reCaptchaService.verifyToken(any(), any())).thenThrow(new ReCaptchaLowScoreException(""));
        setSendResetPasswordEmailMocks();

        // when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(COOKIE_CIP_AUTHDATA_VALID,resetPasswordRequestBody);

        // then
        assertEquals(result.getStatusCode().value(), HttpStatus.ACCEPTED.value());
    }

    @Test
    public void sendResetPasswordEmail_WhenReCaptchaTokenVerificationThrowsReCaptchaUnsuccessfulResponseException_returnBadRequest() throws JSONException,
            ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        when(reCaptchaService.verifyToken(any(), any())).thenThrow(new ReCaptchaUnsuccessfulResponseException(""));
        setSendResetPasswordEmailMocks();

        // when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(COOKIE_CIP_AUTHDATA_VALID,resetPasswordRequestBody);

        // then
        assertEquals(result.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void sendResetPasswordEmail_WhenReCaptchaVerificationIsSuccessful_returnOk() throws JSONException,
            ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(identityAuthorizationService.generateDefaultRedirectSignInUrl()).thenReturn("");
        when(identityAuthorizationService.buildDefaultStateProperty(anyString())).thenReturn("");
        setSendResetPasswordEmailMocks();

        // when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(COOKIE_CIP_AUTHDATA_VALID,resetPasswordRequestBody);

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
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(COOKIE_CIP_AUTHDATA_VALID,resetPasswordRequestBody);

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
        when(cdcResponseHandler.getEmailByUsername(username)).thenReturn(email);
        when(cdcResponseHandler.resetPasswordRequest(username)).thenReturn("");

        //when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(COOKIE_CIP_AUTHDATA_VALID,resetPasswordRequestBody);

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
        when(cdcResponseHandler.getEmailByUsername(username)).thenReturn(email);
        when(cdcResponseHandler.resetPasswordRequest(username)).thenReturn("");
        when(encodeService.encodeBase64(anyString())).thenReturn(COOKIE_CIP_AUTHDATA_VALID.getBytes());

        //when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(new String(),resetPasswordRequestBody);

        //then
        assertEquals(result.getStatusCode(), HttpStatus.OK);
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
        when(cdcResponseHandler.resetPasswordSubmit(mockResetPasswordBody)).thenReturn(resetPasswordResponseMock);
        when(cdcResponseHandler.getAccountInfo(any())).thenReturn(AccountUtils.getSiteAccount());
        doNothing().when(notificationService).sendPasswordUpdateNotification(any());

        //when
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = resetPasswordController.resetPassword(mockResetPasswordBody);

        //then
        assertEquals(resetPasswordResponse.getStatusCode(),HttpStatus.OK);
    }

    @Test
    public void resetPassword_WhenTheSNSHandlerFails_returnINTERNAL_SERVER_ERROR() {
        // given
        ResetPasswordSubmit mockResetPasswordBody = ResetPasswordSubmit.builder()
            .newPassword("testPassword1")
            .resetPasswordToken("testTkn")
            .uid("62623d97356b4815a9965d912fa3331a").build();
        when(resetPasswordResponseMock.getResponseCode()).thenReturn(0);
        when(cdcResponseHandler.resetPasswordSubmit(mockResetPasswordBody)).thenReturn(resetPasswordResponseMock);
        doThrow(NullPointerException.class).when(notificationService).sendPasswordUpdateNotification(any());

        // when
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = resetPasswordController.resetPassword(mockResetPasswordBody);

        // then
        assertEquals(resetPasswordResponse.getStatusCode(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void resetPassword_WhenAnExceptionIsThrown_returnINTERNAL_SERVER_ERROR() {
        //given
        ResetPasswordSubmit mockResetPasswordBody = ResetPasswordSubmit.builder()
            .newPassword("testPassword1")
            .resetPasswordToken("testTkn")
            .uid("62623d97356b4815a9965d912fa3331a").build();
        when(resetPasswordResponseMock.getResponseCode()).thenReturn(0);
        when(cdcResponseHandler.resetPasswordSubmit(any())).thenReturn(resetPasswordResponseMock);
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
        when(cdcResponseHandler.resetPasswordSubmit(any())).thenReturn(resetPasswordResponseMock);


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
        when(cdcResponseHandler.resetPasswordSubmit(any())).thenReturn(resetPasswordResponseMock);


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
        when(cdcResponseHandler.getRP(anyString())).thenReturn(openIdRelyingParty);
        when(encodeService.encodeUTF8(anyString())).thenReturn(URLDecoder.decode(params, StandardCharsets.UTF_8.toString()));
        when(cookieService.createCIPAuthDataCookie(any(), any())).thenReturn(anyString());
        // when
        ResponseEntity<?> response = resetPasswordController.getRPResetPasswordConfig(CLIENT_ID, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.FOUND );
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
        when(cdcResponseHandler.getRP(anyString())).thenReturn(openIdRelyingParty);

        // when
        ResponseEntity<?> response = resetPasswordController.getRPResetPasswordConfig(CLIENT_ID, redirectUtl, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getRPResetPasswordConfig_GivenMethodCalled_WhenParametersAreValidAndClientIdDoesNotExists_ThenShouldReturnBadRequest() throws Exception {
        // given
        when(cdcResponseHandler.getRP(anyString())).thenThrow(new CustomGigyaErrorException("404000"));

        // when
        ResponseEntity<?> response = resetPasswordController.getRPResetPasswordConfig(CLIENT_ID, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getRPResetPasswordConfig_GivenMethodCalled_WhenParametersAreValidAndAErrorOccurred_ThenShouldReturnBadRequest() throws Exception {
        // given
        when(cdcResponseHandler.getRP(anyString())).thenThrow(new CustomGigyaErrorException("599999"));

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
    public void resetPassword_GivenUpdateForRequirePasswordCheck_ResponseShouldBeSuccessful() throws CustomGigyaErrorException {
        // given
        ResetPasswordSubmit mockResetPasswordBody = ResetPasswordSubmit.builder()
            .newPassword("testPassword1")
            .resetPasswordToken("testTkn")
            .uid("62623d97356b4815a9965d912fa3331a").build();
        when(cdcResponseHandler.resetPasswordSubmit(mockResetPasswordBody)).thenReturn(resetPasswordResponseMock);
        doNothing().when(cdcResponseHandler).updateRequirePasswordCheck(any());
        
        // when
        ResponseEntity<ResetPasswordResponse> response = resetPasswordController.resetPassword(mockResetPasswordBody);

        // then
        verify(cdcResponseHandler).updateRequirePasswordCheck(any());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void resetPassword_GivenUpdateRequirePasswordCheckThrowsException_ResponseShouldStillBeSuccessful() throws CustomGigyaErrorException {
        // given
        ResetPasswordSubmit mockResetPasswordBody = ResetPasswordSubmit.builder()
            .newPassword("testPassword1")
            .resetPasswordToken("testTkn")
            .uid("62623d97356b4815a9965d912fa3331a").build();
        when(cdcResponseHandler.resetPasswordSubmit(mockResetPasswordBody)).thenReturn(resetPasswordResponseMock);
        doThrow(CustomGigyaErrorException.class).when(cdcResponseHandler).updateRequirePasswordCheck(any());
        
        // when
        ResponseEntity<ResetPasswordResponse> response = resetPasswordController.resetPassword(mockResetPasswordBody);

        // then
        verify(cdcResponseHandler).updateRequirePasswordCheck(any());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }
}
