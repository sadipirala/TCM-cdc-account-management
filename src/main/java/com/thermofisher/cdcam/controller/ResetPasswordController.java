package com.thermofisher.cdcam.controller;

import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.enums.ResetPasswordErrors;
import com.thermofisher.cdcam.model.*;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.LoginIdDoesNotExistException;
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
import org.springframework.http.HttpHeaders;
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

    @Value("${identity.recaptcha.secret}")
    private String identityReCaptchaSecret;

    @Value("${recaptcha.threshold.minimum}")
    private double RECAPTCHA_MIN_THRESHOLD;

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
    public ResponseEntity<?> sendResetPasswordEmail(@RequestBody ResetPasswordRequest body) throws IOException, JSONException {
        logger.info(String.format("Requested reset password for user: %s", body.getUsername()));

        JSONObject reCaptchaResponse = reCaptchaService.verifyToken(body.getCaptchaToken(), identityReCaptchaSecret);
        logger.info(String.format("Username %s got a %.1f score.", body.getUsername(), reCaptchaResponse.getDouble("score")));
        if (!isReCaptchaResponseValid(reCaptchaResponse)) {
            logger.error(String.format("reCaptcha error for %s. message: %s", body.getUsername(), reCaptchaResponse.toString()));
            return ResponseEntity.badRequest().build();
        }

        try {
            cdcResponseHandler.resetPasswordRequest(body.getUsername());
            logger.info(String.format("Request for reset password successful for: %s", body.getUsername()));
            return ResponseEntity.ok().build();
        } catch (LoginIdDoesNotExistException e) {
            logger.info(e.getMessage());
            return ResponseEntity.ok().build();
        } catch (CustomGigyaErrorException e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    private boolean isReCaptchaResponseValid(JSONObject reCaptchaResponse) throws JSONException {
        final String SUCCESS = "success";
        return reCaptchaResponse.has(SUCCESS) && reCaptchaResponse.getBoolean(SUCCESS) && reCaptchaResponse.getDouble("score") >= RECAPTCHA_MIN_THRESHOLD;
    }

    @PutMapping("/")
    @ApiOperation(value = "resets the password.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody ResetPasswordSubmit body) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ResetPasswordSubmit>> violations = validator.validate(body);
        final int EXPIRED_TOKEN_ERROR = 403025;

        if (violations.size() > 0) {
            logger.error(String.format("One or more errors occurred while creating the reset password object. %s", violations.toArray()));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            ResetPasswordResponse response = cdcResponseHandler.resetPasswordSubmit(body);

            if (response.getResponseCode() != 0) {
                if (response.getResponseCode() == EXPIRED_TOKEN_ERROR) {
                    HttpHeaders responseHeaders = new HttpHeaders();
                    responseHeaders.set("redirect", "/expired");
                    logger.warn(String.format("Expired token, user UID: %s. message: %s", body.getUid(), response.getResponseMessage()));
                    return new ResponseEntity<>(response, responseHeaders, HttpStatus.FOUND);
                } else {
                    logger.warn(String.format("Failed to reset password for user with UID: %s. message: %s", body.getUid(), response.getResponseMessage()));
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }

            String hashedPassword = HashingService.concat(HashingService.hash(body.getNewPassword()));

            ResetPasswordNotification resetPasswordNotification = ResetPasswordNotification.builder()
                    .newPassword(hashedPassword)
                    .uid(body.getUid())
                    .build();

            String message = (new JSONObject(resetPasswordNotification)).toString();

            if (!snsHandler.sendSNSNotification(message, resetPasswordTopic)) {
                logger.warn(String.format("Failed to send SNS notification. topic: %s", resetPasswordTopic));
                return new ResponseEntity<>(createResetPasswordResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),ResetPasswordErrors.SNS_NOT_SEND.getValue()), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            sendResetPasswordConfirmationEmail(body.getUid());
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
