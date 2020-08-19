package com.thermofisher.cdcam.controller;

import com.thermofisher.cdcam.enums.CaptchaErrors;
import com.thermofisher.cdcam.model.ResetPasswordRequest;
import com.thermofisher.cdcam.services.ReCaptchaService;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/reset-password")
public class ResetPasswordController {


    @Autowired
    ReCaptchaService reCaptchaService;

    @Autowired
    CDCResponseHandler cdcResponseHandler;

    @PutMapping("/email")
    @ApiOperation(value = "sends the request to reset a password.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<String> sendResetPasswordEmail(@RequestBody ResetPasswordRequest body) throws IOException, JSONException {
        final String SUCCESS = "success";
        JSONObject verifyResponse = reCaptchaService.verifyToken(body.getCaptchaToken());
        if(verifyResponse.has(SUCCESS) && verifyResponse.getBoolean(SUCCESS)) {
            String email = cdcResponseHandler.getEmailByUsername(body.getUsername());
            verifyResponse.put("loginID",body.getUsername());
            verifyResponse.put("email",email);
            if(!email.isEmpty())
            {
                if(cdcResponseHandler.resetPasswordRequest(body.getUsername())) {
                    return new ResponseEntity<>(verifyResponse.toString(), HttpStatus.OK);
                }
            }
            verifyResponse.put("error-codes",new String[]{CaptchaErrors.CDC_EMAIL_NOT_FOUND.getValue()});
            return new ResponseEntity<>(verifyResponse.toString(), HttpStatus.BAD_REQUEST);
        }
        else {
            verifyResponse.put("loginID",body.getUsername());
            verifyResponse.put("email","");
            return new ResponseEntity<>(verifyResponse.toString(), HttpStatus.BAD_REQUEST);
        }
    }
}
