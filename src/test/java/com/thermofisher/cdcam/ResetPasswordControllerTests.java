package com.thermofisher.cdcam;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.controller.ResetPasswordController;
import com.thermofisher.cdcam.model.ResetPasswordRequest;
import com.thermofisher.cdcam.model.ResetPasswordResponse;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.LoginIdDoesNotExistException;
import com.thermofisher.cdcam.model.ResetPasswordSubmit;
import com.thermofisher.cdcam.services.ReCaptchaService;
import com.thermofisher.cdcam.services.ResetPasswordService;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void sendResetPasswordEmail_WhenATokenIsInvalid_returnBadRequest() throws JSONException, IOException {
        // given
        String invalidToken = "test";
        String username = "arminvalidtest@mail.com";
        ResetPasswordRequest mockInvalidBody = Mockito.mock(ResetPasswordRequest.class);
        JSONObject mockResponseJson = new JSONObject();
        mockResponseJson.put("success", false);
        when(mockInvalidBody.getUsername()).thenReturn(username);
        when(mockInvalidBody.getCaptchaToken()).thenReturn(invalidToken);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(mockResponseJson);

        // when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(mockInvalidBody);

        // then
        Assert.assertEquals(result.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void sendResetPasswordEmail_WhenATokenIsValidAndTheAccountDoesNotExistsInCDC_returnOk()
            throws JSONException, IOException {
        // given
        String validToken = "test";
        ResetPasswordRequest mockInvalidBody = Mockito.mock(ResetPasswordRequest.class);
        JSONObject mockResponseJson = new JSONObject();
        mockResponseJson.put("success", true);

        when(mockInvalidBody.getCaptchaToken()).thenReturn(validToken);
        when(reCaptchaService.verifyToken(any(), any())).thenReturn(mockResponseJson);

        // when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(mockInvalidBody);

        // then
        Assert.assertEquals(result.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void sendResetPasswordEmail_WhenATokenIsValidAndTheAccountExistinCDC_returnOK()
            throws JSONException, IOException, CustomGigyaErrorException, LoginIdDoesNotExistException {
        //given
        String validToken = "test";
        String username = "armvalidtest@mail.com";
        String email = "armemailtest@mail.com";

        ResetPasswordRequest mockInvalidBody = Mockito.mock(ResetPasswordRequest.class);
        JSONObject mockResponseJson = new JSONObject();
        mockResponseJson.put("success", true);

        when(mockInvalidBody.getUsername()).thenReturn(username);
        when(mockInvalidBody.getCaptchaToken()).thenReturn(validToken);
        when(reCaptchaService.verifyToken(any(),any())).thenReturn(mockResponseJson);
        when(cdcResponseHandler.getEmailByUsername(username)).thenReturn(email);
        doNothing().when(cdcResponseHandler).resetPasswordRequest(username);

        //when
        ResponseEntity<?> result = resetPasswordController.sendResetPasswordEmail(mockInvalidBody);

        //then
        Assert.assertEquals(result.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void resetPassword_WhenAValidBodyIsSent_ShouldTriggerRequestForResetPasswordConfirmationEmail_AndReturnOK() throws IOException {
        //given
        ResetPasswordSubmit mockResetPasswordBody = ResetPasswordSubmit.builder()
                .newPassword("testPassword1")
                .resetPasswordToken("testTkn")
                .uid("62623d97356b4815a9965d912fa3331a").build();
        ResetPasswordResponse mockResponse = Mockito.mock(ResetPasswordResponse.class);
        when(mockResponse.getResponseCode()).thenReturn(0);
        when(cdcResponseHandler.resetPasswordSubmit(mockResetPasswordBody)).thenReturn(mockResponse);
        when(cdcResponseHandler.getAccountInfo(any())).thenReturn(AccountUtils.getSiteAccount());
        when(snsHandler.sendSNSNotification(any(),any())).thenReturn(true);

        doNothing().when(resetPasswordService).sendResetPasswordConfirmation(any());

        //when
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = resetPasswordController.resetPassword(mockResetPasswordBody);

        //then
        Mockito.verify(resetPasswordService).sendResetPasswordConfirmation(any());
        Assert.assertEquals(resetPasswordResponse.getStatusCode(),HttpStatus.OK);
    }

    @Test
    public void resetPassword_WhenTheSNSHandlerFails_returnINTERNAL_SERVER_ERROR() {

        //given
        ResetPasswordSubmit mockResetPasswordBody = ResetPasswordSubmit.builder()
                .newPassword("testPassword1")
                .resetPasswordToken("testTkn")
                .uid("62623d97356b4815a9965d912fa3331a").build();
        ResetPasswordResponse mockResponse = Mockito.mock(ResetPasswordResponse.class);
        when(mockResponse.getResponseCode()).thenReturn(0);
        when(cdcResponseHandler.resetPasswordSubmit(mockResetPasswordBody)).thenReturn(mockResponse);
        when(snsHandler.sendSNSNotification(any(),any())).thenReturn(false);

        //when
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = resetPasswordController.resetPassword(mockResetPasswordBody);

        //then
        Assert.assertEquals(resetPasswordResponse.getStatusCode(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void resetPassword_WhenAnExceptionIsThrown_returnINTERNAL_SERVER_ERROR() {
        //given
        ResetPasswordSubmit mockResetPasswordBody = ResetPasswordSubmit.builder()
                .newPassword("testPassword1")
                .resetPasswordToken("testTkn")
                .uid("62623d97356b4815a9965d912fa3331a").build();
        ResetPasswordResponse mockResponse = Mockito.mock(ResetPasswordResponse.class);
        when(mockResponse.getResponseCode()).thenReturn(0);
        when(cdcResponseHandler.resetPasswordSubmit(any())).thenReturn(mockResponse);

        //when
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = resetPasswordController.resetPassword(mockResetPasswordBody);

        //then
        Assert.assertEquals(resetPasswordResponse.getStatusCode(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void resetPassword_WhenTheResetPasswordOnCDCFails_returnBAD_REQUEST() {
        //given
        ResetPasswordSubmit mockResetPasswordBody = ResetPasswordSubmit.builder()
                .newPassword("testPassword1")
                .resetPasswordToken("testTkn")
                .uid("62623d97356b4815a9965d912fa3331a").build();
        ResetPasswordResponse mockResponse = Mockito.mock(ResetPasswordResponse.class);
        when(mockResponse.getResponseCode()).thenReturn(40001);
        when(cdcResponseHandler.resetPasswordSubmit(any())).thenReturn(mockResponse);


        //when
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = resetPasswordController.resetPassword(mockResetPasswordBody);

        //then
        Assert.assertEquals(resetPasswordResponse.getStatusCode(),HttpStatus.BAD_REQUEST);
    }
}
