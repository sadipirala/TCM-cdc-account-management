package com.thermofisher.cdcam;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.controller.ResetPasswordController;
import com.thermofisher.cdcam.model.ResetPasswordRequest;
import com.thermofisher.cdcam.model.ResetPasswordResponse;
import com.thermofisher.cdcam.model.ResetPasswordSubmit;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.LoginIdDoesNotExistException;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaLowScoreException;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaUnsuccessfulResponseException;
import com.thermofisher.cdcam.services.ReCaptchaService;
import com.thermofisher.cdcam.services.ResetPasswordService;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;

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
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class ResetPasswordControllerTests {
    private final String reCaptchaV3Secret = "reCaptchaV3Secret";
    private final String reCaptchaV2Secret = "reCaptchaV2Secret";

    String username = "armadillo@mail.com";
    String email = "armadillo@mail.com";
    JSONObject reCaptchaResponse;
    ResetPasswordRequest resetPasswordRequestBody;
    ResetPasswordResponse resetPasswordResponseMock;

    @InjectMocks
    ResetPasswordController resetPasswordController;

    @Mock
    ReCaptchaService reCaptchaService;

    @Mock
    CDCResponseHandler cdcResponseHandler;

    @Mock
    SNSHandler snsHandler;

    @Mock
    ResetPasswordService resetPasswordService;

    @Captor
    ArgumentCaptor<String> reCaptchaSecretCaptor;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        reCaptchaResponse = new JSONObject();
        resetPasswordRequestBody = mock(ResetPasswordRequest.class);
        resetPasswordResponseMock = mock(ResetPasswordResponse.class);
        ReflectionTestUtils.setField(resetPasswordController, "identityReCaptchaSecretV3", reCaptchaV3Secret);
        ReflectionTestUtils.setField(resetPasswordController, "identityReCaptchaSecretV2", reCaptchaV2Secret);
    }

    private void setSendResetPasswordEmailMocks()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException {
        when(resetPasswordRequestBody.getUsername()).thenReturn(username);
        when(resetPasswordRequestBody.getCaptchaToken()).thenReturn("token");
    }

    @Test
    public void sendResetPasswordEmail_givenReCaptchaVersionIsV2_ThenReCaptchaServiceShouldGetCalledWithReCaptchaV2Secret()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, IOException {
        // given
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        when(resetPasswordRequestBody.getIsReCaptchaV2()).thenReturn(true);
        setSendResetPasswordEmailMocks();

        // when
        resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody);

        // then
        verify(reCaptchaService).verifyToken(anyString(), reCaptchaSecretCaptor.capture());
        String reCaptchaSecret = reCaptchaSecretCaptor.getValue();
        assertEquals(reCaptchaSecret, reCaptchaV2Secret);
    }

    @Test
    public void sendResetPasswordEmail_givenReCaptchaVersionIsV3_ThenReCaptchaServiceShouldGetCalledWithReCaptchaV3Secret()
            throws JSONException, ReCaptchaLowScoreException, ReCaptchaUnsuccessfulResponseException, IOException {
        // given
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(reCaptchaResponse);
        setSendResetPasswordEmailMocks();

        // when
        resetPasswordController.sendResetPasswordEmail(resetPasswordRequestBody);

        // then
        verify(reCaptchaService).verifyToken(anyString(), reCaptchaSecretCaptor.capture());
        String reCaptchaSecret = reCaptchaSecretCaptor.getValue();
        assertEquals(reCaptchaSecret, reCaptchaV3Secret);
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
            .uid("62623d97356b4815a9965d912fa3331a").build();
        when(resetPasswordResponseMock.getResponseCode()).thenReturn(0);
        when(cdcResponseHandler.resetPasswordSubmit(mockResetPasswordBody)).thenReturn(resetPasswordResponseMock);
        when(cdcResponseHandler.getAccountInfo(any())).thenReturn(AccountUtils.getSiteAccount());
        doNothing().when(snsHandler).sendNotification(any(),any());
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
        doThrow(NullPointerException.class).when(snsHandler).sendNotification(any(), any());

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
        doThrow(NullPointerException.class).when(snsHandler).sendNotification(any(), any());

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
}
