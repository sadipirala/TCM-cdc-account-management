package com.thermofisher.cdcam.controller;

import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.enums.ResetPasswordErrors;
import com.thermofisher.cdcam.model.*;
import com.thermofisher.cdcam.services.ReCaptchaService;
import com.thermofisher.cdcam.services.ResetPasswordService;
import com.thermofisher.cdcam.services.hashing.HashingService;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/reset-password")
public class ResetPasswordController {
    private Logger logger = LogManager.getLogger(this.getClass());

    @Value("${aws.sns.reset.password}")
    private String resetPasswordTopic;

    @Value("${registration.recaptcha.secret.key}")
    private String registrationReCaptchaSecret;

    @Autowired
    ReCaptchaService reCaptchaService;

    @Autowired
    CDCResponseHandler cdcResponseHandler;

    @Autowired
    SNSHandler snsHandler;

    @Autowired
    ResetPasswordService resetPasswordService;

    @PostMapping("/email")
    @ApiOperation(value = "sends the request to reset a password.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<String> sendResetPasswordEmail(@RequestBody ResetPasswordRequest body) throws IOException, JSONException {
        final String SUCCESS = "success";
        JSONObject verifyResponse = reCaptchaService.verifyToken(body.getCaptchaToken(),registrationReCaptchaSecret);
        verifyResponse.put("loginID", body.getUsername());
        if (verifyResponse.has(SUCCESS) && verifyResponse.getBoolean(SUCCESS)) {
            String email = cdcResponseHandler.getEmailByUsername(body.getUsername());
            verifyResponse.put("email", email);
            if (!email.isEmpty()) {
                if (cdcResponseHandler.resetPasswordRequest(body.getUsername())) {
                    return new ResponseEntity<>(verifyResponse.toString(), HttpStatus.OK);
                }
            }
            verifyResponse.put("error-codes", new String[]{ResetPasswordErrors.CDC_EMAIL_NOT_FOUND.getValue()});
            return new ResponseEntity<>(verifyResponse.toString(), HttpStatus.BAD_REQUEST);
        } else {
            verifyResponse.put("email", "");
            return new ResponseEntity<>(verifyResponse.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/")
    @ApiOperation(value = "resets the password.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody ResetPassword body) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ResetPassword>> violations = validator.validate(body);

        if (violations.size() > 0) {
            logger.error(String.format("One or more errors occurred while creating the reset password object. %s", violations.toArray()));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            String accountUID = cdcResponseHandler.getUIDByUsername(body.getUsername());

            if (accountUID.isEmpty()) {
                return new ResponseEntity<>(createResetPasswordResponse(HttpStatus.BAD_REQUEST.value(),ResetPasswordErrors.ACCOUNT_NOT_FOUND.getValue()), HttpStatus.BAD_REQUEST);
            }

            ResetPasswordResponse response = cdcResponseHandler.resetPassword(body);

            if (response.getResponseCode() != 0) {
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            String hashedPassword = HashingService.concat(HashingService.hash(body.getNewPassword()));

            ResetPasswordNotification resetPasswordNotification = ResetPasswordNotification.builder()
                    .newPassword(hashedPassword)
                    .uid(accountUID)
                    .build();

            String message = (new JSONObject(resetPasswordNotification)).toString();

            if (!snsHandler.sendSNSNotification(message, resetPasswordTopic)) {
                return new ResponseEntity<>(createResetPasswordResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),ResetPasswordErrors.SNS_NOT_SEND.getValue()), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            sendResetPasswordConfirmationEmail(accountUID);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(createResetPasswordResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResetPasswordResponse createResetPasswordResponse(int responseCode,String responseMessage) {
        return ResetPasswordResponse.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage).build();
    }

    private void sendResetPasswordConfirmationEmail(String uid) throws IOException {
        AccountInfo account = cdcResponseHandler.getAccountInfo(uid);
        resetPasswordService.sendResetPasswordConfirmation(account);
    }
}
