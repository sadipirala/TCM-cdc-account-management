package com.thermofisher.cdcam;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.controller.ResetPasswordController;
import com.thermofisher.cdcam.model.ResetPasswordRequest;
import com.thermofisher.cdcam.services.ReCaptchaService;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;
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
        when(reCaptchaService.verifyToken(invalidToken)).thenReturn(mockResponseJson);

        //when
        ResponseEntity<JSONObject> result = resetPasswordController.sendResetPasswordEmail(mockInvalidBody);

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
        when(reCaptchaService.verifyToken(validToken)).thenReturn(mockResponseJson);
        when(cdcResponseHandler.getEmailByUsername(username)).thenReturn("");

        //when
        ResponseEntity<JSONObject> result = resetPasswordController.sendResetPasswordEmail(mockInvalidBody);

        JSONObject errors = result.getBody();
        String[] stringArray = (String[])errors.get("error-codes");

        //then
        Assert.assertEquals(result.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertTrue( stringArray.length > 0);
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
        when(reCaptchaService.verifyToken(validToken)).thenReturn(mockResponseJson);
        when(cdcResponseHandler.getEmailByUsername(username)).thenReturn(email);
        when(cdcResponseHandler.resetPasswordRequest(username)).thenReturn(true);

        //when
        ResponseEntity<JSONObject> result = resetPasswordController.sendResetPasswordEmail(mockInvalidBody);

        //then
        Assert.assertEquals(result.getStatusCode(), HttpStatus.OK);
    }
}
