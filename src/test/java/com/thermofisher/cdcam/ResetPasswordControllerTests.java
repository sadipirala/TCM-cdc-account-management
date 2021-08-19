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
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
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
    private String COOKIE_CIP_AUTHDATA_INVALID = "eyJyZWRpcmVjdFVyaSI6InJlZGlyZWN0VXJpIiwic3RhdGUiOiJzdGF0ZSIsInNjb3BlIjoic2NvcGUiLCJyZXNwb25zZVR5cGUiOiJyZXNwb25zZVR5cGUifQ==";
    private boolean IS_SIGN_IN_URL = true;

    @InjectMocks
    ResetPasswordController resetPasswordController;

    @Mock
    CDCResponseHandler cdcResponseHandler;

    @Mock
    NotificationService notificationService;

    @Mock
    ReCaptchaService reCaptchaService;

    @Mock
    ResetPasswordService resetPasswordService;

    @Mock
    SecretsService secretsService;

    @Mock
    EncodeService encodeService;

    @Mock
    CookieService cookieService;
    
    @Captor
    ArgumentCaptor<String> reCaptchaSecretCaptor;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        reCaptchaResponse = new JSONObject();
        resetPasswordRequestBody = mock(ResetPasswordRequest.class);
        resetPasswordResponseMock = mock(ResetPasswordResponse.class);
    }

    private void setSendResetPasswordEmailMocks() throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        when(resetPasswordRequestBody.getUsername()).thenReturn(username);
        when(resetPasswordRequestBody.getCaptchaToken()).thenReturn("token");
    }

    @Test
    public void sendResetPasswordEmail_givenReCaptchaVersionIsV2_ThenReCaptchaServiceShouldGetCalledWithReCaptchaV2Secret()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, IOException {
        // given
        String expectedReCaptchaV2Secret = RandomStringUtils.random(10);
        when(secretsService.get(CdcamSecrets.RECAPTCHAV2.getKey())).thenReturn(expectedReCaptchaV2Secret);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(resetPasswordRequestBody.getIsReCaptchaV2()).thenReturn(true);
        setSendResetPasswordEmailMocks();

        // when
        resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody);

        // then
        verify(reCaptchaService).verifyToken(anyString(), reCaptchaSecretCaptor.capture());
        String reCaptchaSecret = reCaptchaSecretCaptor.getValue();
        assertEquals(expectedReCaptchaV2Secret, reCaptchaSecret);
    }

    @Test
    public void sendResetPasswordEmail_givenReCaptchaVersionIsV3_ThenReCaptchaServiceShouldGetCalledWithReCaptchaV3Secret()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, IOException {
        // given
        String expectedReCaptchaV3Secret = RandomStringUtils.random(10);
        when(secretsService.get(CdcamSecrets.RECAPTCHAV3.getKey())).thenReturn(expectedReCaptchaV3Secret);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        setSendResetPasswordEmailMocks();

        // when
        resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody);

        // then
        verify(reCaptchaService).verifyToken(anyString(), reCaptchaSecretCaptor.capture());
        String reCaptchaSecret = reCaptchaSecretCaptor.getValue();
        assertEquals(expectedReCaptchaV3Secret, reCaptchaSecret);
    }

    @Test
    public void sendResetPasswordEmail_WhenReCaptchaTokenVerificationThrowsReCaptchaLowScoreException_returnAccepted() throws JSONException, IOException,
            ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        when(reCaptchaService.verifyToken(any(), any())).thenThrow(new ReCaptchaLowScoreException(""));
        setSendResetPasswordEmailMocks();

        // when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody);

        // then
        assertEquals(result.getStatusCode().value(), HttpStatus.ACCEPTED.value());
    }

    @Test
    public void sendResetPasswordEmail_WhenReCaptchaTokenVerificationThrowsReCaptchaUnsuccessfulResponseException_returnBadRequest() throws JSONException, IOException,
            ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        when(reCaptchaService.verifyToken(any(), any())).thenThrow(new ReCaptchaUnsuccessfulResponseException(""));
        setSendResetPasswordEmailMocks();

        // when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody);

        // then
        assertEquals(result.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void sendResetPasswordEmail_WhenReCaptchaVerificationIsSuccessful_returnOk() throws JSONException, IOException,
            ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        setSendResetPasswordEmailMocks();

        // when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody);

        // then
        assertEquals(result.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void sendResetPasswordEmail_WhenATokenIsValidAndTheAccountDoesNotExistsInCDC_returnOk()
            throws JSONException, IOException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        // given
        when(resetPasswordRequestBody.getCaptchaToken()).thenReturn("token");
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);

        // when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody);

        // then
        assertEquals(result.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void sendResetPasswordEmail_WhenATokenIsValidAndTheAccountExistinCDC_returnOK()
            throws JSONException, IOException, CustomGigyaErrorException, LoginIdDoesNotExistException,
            ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        //given
        setSendResetPasswordEmailMocks();
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(cdcResponseHandler.getEmailByUsername(username)).thenReturn(email);
        doNothing().when(cdcResponseHandler).resetPasswordRequest(username);

        //when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody);

        //then
        assertEquals(result.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void resetPassword_WhenAValidBodyIsSent_ShouldTriggerRequestForResetPasswordConfirmationEmail_AndReturnOK() throws IOException, CustomGigyaErrorException {
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
        doNothing().when(resetPasswordService).sendResetPasswordConfirmation(any());

        //when
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = resetPasswordController.resetPassword(mockResetPasswordBody);

        //then
        verify(resetPasswordService).sendResetPasswordConfirmation(any());
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
    public void redirectAuth_GivenMethodCalled_WhenParametersAreValid_ThenShouldReturnFoundStatusAndCIP_AUTDATAShouldBePresentInHeaders() throws Exception {
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
        ResponseEntity<?> response = resetPasswordController.redirectAuth(CLIENT_ID, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.FOUND );
        assertTrue(!Utils.isNullOrEmpty(response.getHeaders().get("Set-Cookie")));
        assertTrue(!Utils.isNullOrEmpty(response.getHeaders().get("Location")));
    }

    @Test
    public void redirectAuth_GivenMethodCalled_WhenParametersAreValidAndURiDoesNotExistInClientURIs_ThenShouldReturnBadRequest() throws Exception {
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
        ResponseEntity<?> response = resetPasswordController.redirectAuth(CLIENT_ID, redirectUtl, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void redirectAuth_GivenMethodCalled_WhenParametersAreValidAndClientIdDoesNotExists_ThenShouldReturnBadRequest() throws Exception {
        // given
        when(cdcResponseHandler.getRP(anyString())).thenThrow(new CustomGigyaErrorException("404000"));

        // when
        ResponseEntity<?> response = resetPasswordController.redirectAuth(CLIENT_ID, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void redirectAuth_GivenMethodCalled_WhenParametersAreValidAndAErrorOccurred_ThenShouldReturnBadRequest() throws Exception {
        // given
        when(cdcResponseHandler.getRP(anyString())).thenThrow(new CustomGigyaErrorException("599999"));

        // when
        ResponseEntity<?> response = resetPasswordController.redirectAuth(CLIENT_ID, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void redirectAuth_GivenMethodCalled_WhenClientIDIsNullOrEmpty_ThenShouldReturnBadRequest() throws Exception {
        // when
        ResponseEntity<?> response = resetPasswordController.redirectAuth(null, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void redirectAuth_GivenMethodCalled_WhenRedirectURLIsNullOrEmpty_ThenShouldReturnBadRequest() throws Exception {
        // when
        ResponseEntity<?> response = resetPasswordController.redirectAuth(CLIENT_ID, "", STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }
}
