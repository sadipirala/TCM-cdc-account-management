package com.thermofisher.cdcam.controller;

import java.io.IOException;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import com.thermofisher.cdcam.enums.aws.CdcamSecrets;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.ResetPasswordRequest;
import com.thermofisher.cdcam.model.ResetPasswordResponse;
import com.thermofisher.cdcam.model.ResetPasswordSubmit;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.LoginIdDoesNotExistException;
import com.thermofisher.cdcam.model.notifications.PasswordUpdateNotification;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaLowScoreException;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaUnsuccessfulResponseException;
import com.thermofisher.cdcam.services.NotificationService;
import com.thermofisher.cdcam.services.ReCaptchaService;
import com.thermofisher.cdcam.services.ResetPasswordService;
import com.thermofisher.cdcam.services.SecretsService;
import com.thermofisher.cdcam.services.hashing.HashingService;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/reset-password")
public class ResetPasswordController {
    private Logger logger = LogManager.getLogger(this.getClass());

    @Value("${aws.sns.password.update}")
    private String passwordUpdateTopic;

    @Autowired
    CDCResponseHandler cdcResponseHandler;

    @Autowired
    NotificationService notificationService;

    @Autowired
    ReCaptchaService reCaptchaService;

    @Autowired
    ResetPasswordService resetPasswordService;

    @Autowired
    SecretsService secretsService;

    @PostMapping("/email")
    @ApiOperation(value = "sends the request to reset a password.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<?> sendResetPasswordEmail(@RequestBody ResetPasswordRequest body) {
        logger.info(String.format("Requested reset password for user: %s", body.getUsername()));

        try {
            String reCaptchaSecretKey = body.getIsReCaptchaV2() ? CdcamSecrets.RECAPTCHAV2.getKey() : CdcamSecrets.RECAPTCHAV3.getKey();
            String reCaptchaSecret = secretsService.get(reCaptchaSecretKey);
            JSONObject reCaptchaResponse = reCaptchaService.verifyToken(body.getCaptchaToken(), reCaptchaSecret);
            logger.info(String.format("reCaptcha response for %s: %s", body.getUsername(), reCaptchaResponse.toString()));
            cdcResponseHandler.resetPasswordRequest(body.getUsername());
            logger.info(String.format("Request for reset password successful for: %s", body.getUsername()));
            return ResponseEntity.ok().build();
        } catch (LoginIdDoesNotExistException e) {
            logger.info(e.getMessage());
            return ResponseEntity.ok().build();
        } catch (CustomGigyaErrorException e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (ReCaptchaLowScoreException v3Exception) {
            logger.error(String.format("reCaptcha v3 error for: %s. message: %s", body.getUsername(), v3Exception.getMessage()));
            return ResponseEntity.accepted().build();
        } catch (ReCaptchaUnsuccessfulResponseException v2Exception) {
            logger.error(String.format("reCaptcha v2 error for: %s. message: %s", body.getUsername(), v2Exception.getMessage()));
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error(String.format("Error: %s", e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

            String hashedPassword = HashingService.toMD5(body.getNewPassword());
            PasswordUpdateNotification passwordUpdateNotification = PasswordUpdateNotification.builder()
                .newPassword(hashedPassword)
                .uid(body.getUid())
                .build();
            notificationService.sendPasswordUpdateNotification(passwordUpdateNotification);
            sendResetPasswordConfirmationEmail(body.getUid());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ResetPasswordResponse response = createResetPasswordResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResetPasswordResponse createResetPasswordResponse(int responseCode,String responseMessage) {
        return ResetPasswordResponse.builder()
            .responseCode(responseCode)
            .responseMessage(responseMessage)
            .build();
    }

    private void sendResetPasswordConfirmationEmail(String uid) throws IOException, CustomGigyaErrorException {
        AccountInfo account = cdcResponseHandler.getAccountInfo(uid);
        resetPasswordService.sendResetPasswordConfirmation(account);
    }
}
