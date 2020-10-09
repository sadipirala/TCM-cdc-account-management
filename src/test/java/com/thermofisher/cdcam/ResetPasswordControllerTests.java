package com.thermofisher.cdcam;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.controller.ResetPasswordController;
import com.thermofisher.cdcam.model.ResetPassword;
import com.thermofisher.cdcam.model.ResetPasswordRequest;
import com.thermofisher.cdcam.model.ResetPasswordResponse;
import com.thermofisher.cdcam.services.ReCaptchaService;
import com.thermofisher.cdcam.services.ResetPasswordService;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.junit.Assert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

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
        //given
        String invalidToken = "test";
        String username = "arminvalidtest@mail.com";
        ResetPasswordRequest mockInvalidBody = Mockito.mock(ResetPasswordRequest.class);
        JSONObject mockResponseJson = new JSONObject();
        mockResponseJson.put("success", false);
        when(mockInvalidBody.getUsername()).thenReturn(username);
        when(mockInvalidBody.getCaptchaToken()).thenReturn(invalidToken);
        when(reCaptchaService.verifyToken(any(),any())).thenReturn(mockResponseJson);

        //when
        ResponseEntity<String> result = resetPasswordController.sendResetPasswordEmail(mockInvalidBody);

        //then
        Assert.assertEquals(result.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void sendResetPasswordEmail_WhenATokenIsValidAndTheAccountDoesNotExistinCDC_returnBadRequest() throws JSONException, IOException {
        //given
        String validToken = "test";
        String username = "arminvalidtest@mail.com";

        ResetPasswordRequest mockInvalidBody = Mockito.mock(ResetPasswordRequest.class);
        JSONObject mockResponseJson = new JSONObject();
        mockResponseJson.put("success", true);

        when(mockInvalidBody.getUsername()).thenReturn(username);
        when(mockInvalidBody.getCaptchaToken()).thenReturn(validToken);
        when(reCaptchaService.verifyToken(any(),any())).thenReturn(mockResponseJson);
        when(cdcResponseHandler.getEmailByUsername(username)).thenReturn("");

        //when
        ResponseEntity<String> result = resetPasswordController.sendResetPasswordEmail(mockInvalidBody);

        JSONObject errors = new JSONObject(result.getBody());
        JSONArray stringArray = (JSONArray)errors.get("error-codes");

        //then
        Assert.assertEquals(result.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertTrue( stringArray.length() > 0);
    }

    @Test
    public void sendResetPasswordEmail_WhenATokenIsValidAndTheAccountExistinCDC_returnOK() throws JSONException, IOException {
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
        when(cdcResponseHandler.resetPasswordRequest(username)).thenReturn(true);

        //when
        ResponseEntity<String> result = resetPasswordController.sendResetPasswordEmail(mockInvalidBody);

        //then
        Assert.assertEquals(result.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void resetPassword_WhenAValidBodyIsSent_ShouldTriggerRequestForResetPasswordConfirmationEmail_AndReturnOK() throws IOException {
        //given
        ResetPassword mockResetPasswordBody = ResetPassword.builder()
                .newPassword("testPassword1")
                .resetPasswordToken("testTkn")
                .username("test@tes.com").build();
        ResetPasswordResponse mockResponse = Mockito.mock(ResetPasswordResponse.class);
        when(mockResponse.getResponseCode()).thenReturn(0);
        when(cdcResponseHandler.resetPassword(mockResetPasswordBody)).thenReturn(mockResponse);
        when(cdcResponseHandler.getUIDByUsername(any())).thenReturn("testUID");
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
    public void resetPassword_WhenTheSNSHandlerFails_returnINTERNAL_SERVER_ERROR() throws IOException {

        //given
        ResetPassword mockResetPasswordBody = ResetPassword.builder()
                .newPassword("testPassword1")
                .resetPasswordToken("testTkn")
                .username("test@tes.com").build();
        ResetPasswordResponse mockResponse = Mockito.mock(ResetPasswordResponse.class);
        when(mockResponse.getResponseCode()).thenReturn(0);
        when(cdcResponseHandler.resetPassword(mockResetPasswordBody)).thenReturn(mockResponse);
        when(cdcResponseHandler.getUIDByUsername(any())).thenReturn("testUID");
        when(snsHandler.sendSNSNotification(any(),any())).thenReturn(false);

        //when
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = resetPasswordController.resetPassword(mockResetPasswordBody);

        //then
        Assert.assertEquals(resetPasswordResponse.getStatusCode(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void resetPassword_WhenAnExceptionIsThrown_returnINTERNAL_SERVER_ERROR() throws IOException {
        //given
        ResetPassword mockResetPasswordBody = ResetPassword.builder()
                .newPassword("testPassword1")
                .resetPasswordToken("testTkn")
                .username("test@tes.com").build();
        ResetPasswordResponse mockResponse = Mockito.mock(ResetPasswordResponse.class);
        when(mockResponse.getResponseCode()).thenReturn(0);
        when(cdcResponseHandler.resetPassword(any())).thenReturn(mockResponse);
        when(cdcResponseHandler.getUIDByUsername(any())).thenReturn(null);

        //when
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = resetPasswordController.resetPassword(mockResetPasswordBody);

        //then
        Assert.assertEquals(resetPasswordResponse.getStatusCode(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void resetPassword_WhenTheResetPasswordOnCDCFails_returnBAD_REQUEST() throws IOException {
        //given
        ResetPassword mockResetPasswordBody = ResetPassword.builder()
                .newPassword("testPassword1")
                .resetPasswordToken("testTkn")
                .username("test@tes.com").build();
        ResetPasswordResponse mockResponse = Mockito.mock(ResetPasswordResponse.class);
        when(cdcResponseHandler.getUIDByUsername(any())).thenReturn("testUID");
        when(mockResponse.getResponseCode()).thenReturn(40001);
        when(cdcResponseHandler.resetPassword(any())).thenReturn(mockResponse);


        //when
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = resetPasswordController.resetPassword(mockResetPasswordBody);

        //then
        Assert.assertEquals(resetPasswordResponse.getStatusCode(),HttpStatus.BAD_REQUEST);
    }

    @Test
    public void resetPassword_WhenNoAccountIsFoundByUsername_returnBAD_REQUEST() throws IOException {
        //given
        ResetPassword mockResetPasswordBody = ResetPassword.builder()
                .newPassword("testPassword1")
                .resetPasswordToken("testTkn")
                .username("test@tes.com").build();
        ResetPasswordResponse mockResponse = Mockito.mock(ResetPasswordResponse.class);
        when(mockResponse.getResponseCode()).thenReturn(0);
        when(cdcResponseHandler.resetPassword(mockResetPasswordBody)).thenReturn(mockResponse);
        when(cdcResponseHandler.getUIDByUsername(any())).thenReturn("");

        //when
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = resetPasswordController.resetPassword(mockResetPasswordBody);

        //then
        Assert.assertEquals(resetPasswordResponse.getStatusCode(),HttpStatus.BAD_REQUEST);
    }
}
