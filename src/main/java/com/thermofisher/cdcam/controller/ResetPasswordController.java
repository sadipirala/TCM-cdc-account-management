package com.thermofisher.cdcam.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import com.gigya.socialize.GSKeyNotFoundException;
import com.thermofisher.cdcam.enums.CookieType;
import com.thermofisher.cdcam.enums.aws.CdcamSecrets;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.ResetPasswordRequest;
import com.thermofisher.cdcam.model.ResetPasswordResponse;
import com.thermofisher.cdcam.model.ResetPasswordSubmit;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.LoginIdDoesNotExistException;
import com.thermofisher.cdcam.model.cdc.OpenIdRelyingParty;
import com.thermofisher.cdcam.model.dto.CIPAuthDataDTO;
import com.thermofisher.cdcam.model.dto.RequestResetPasswordDTO;
import com.thermofisher.cdcam.model.notifications.PasswordUpdateNotification;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaLowScoreException;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaUnsuccessfulResponseException;
import com.thermofisher.cdcam.services.*;
import com.thermofisher.cdcam.services.hashing.HashingService;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;

import io.swagger.annotations.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/identity/reset-password")
public class ResetPasswordController {
    private Logger logger = LogManager.getLogger(this.getClass());
    private final String REQUEST_EXCEPTION_HEADER = "Request-Exception";

    @Value("${identity.reset-password.get-login-endpoint.path}")
    private String getOidcLoginEndpointPath;

    @Value("${identity.reset-password.redirect_uri}")
    private String rpRedirectUri;

    @Autowired
    CDCResponseHandler cdcResponseHandler;

    @Autowired
    NotificationService notificationService;

    @Autowired
    ReCaptchaService reCaptchaService;

    @Autowired
    SecretsService secretsService;

    @Autowired
    IdentityAuthorizationService identityAuthorizationService;

    @Autowired
    CookieService cookieService;

    @Autowired
    EncodeService encodeService;

    @Autowired
    URLService urlService;

    @PostMapping("/email")
    @ApiOperation(value = "sends the request to reset a password.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<?> sendResetPasswordEmail(@CookieValue(name = "cip_authdata", required = false) String cipAuthData, @RequestBody ResetPasswordRequest body) {
        logger.info(String.format("Requested reset password for user: %s", body.getUsername()));
        if (Utils.isNullOrEmpty(cipAuthData)) {
            logger.info("Cookie not present.");
            cipAuthData = cookieService.buildDefaultCipAuthDataCookie(CookieType.RESET_PASSWORD);
        }
        try {
            String reCaptchaSecretKey = body.getIsReCaptchaV2() ? CdcamSecrets.RECAPTCHAV2.getKey() : CdcamSecrets.RECAPTCHAV3.getKey();
            String reCaptchaSecret = secretsService.get(reCaptchaSecretKey);
            JSONObject reCaptchaResponse = reCaptchaService.verifyToken(body.getCaptchaToken(), reCaptchaSecret);
            logger.info(String.format("reCaptcha response for %s: %s", body.getUsername(), reCaptchaResponse.toString()));
            String passwordToken = cdcResponseHandler.resetPasswordRequest(body.getUsername());
            logger.info(String.format("Request reset password was successfully for: %s", body.getUsername()));
            RequestResetPasswordDTO requestResetPasswordDTO = RequestResetPasswordDTO.builder()
                    .passwordToken(passwordToken)
                    .authData(cipAuthData)
                    .build();

            sendRequestResetPasswordEmail(body.getUsername(), requestResetPasswordDTO);

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
        logger.info(String.format("Reset password process started for: %s", body.getUid()));
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

            try {
                cdcResponseHandler.updateRequirePasswordCheck(body.getUid());
            } catch (CustomGigyaErrorException e) {
                logger.error(e.getMessage());
            }

            logger.info(String.format("Build password update notification started for: %s", body.getUid()));
            String hashedPassword = HashingService.toMD5(body.getNewPassword());
            PasswordUpdateNotification passwordUpdateNotification = PasswordUpdateNotification.builder()
                .newPassword(hashedPassword)
                .uid(body.getUid())
                .build();
            notificationService.sendPasswordUpdateNotification(passwordUpdateNotification);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(String.format("An exception occurred: %s",e.getMessage()));
            ResetPasswordResponse response = createResetPasswordResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = {"/oidc/rp", "/rp"})
    @ApiOperation(value = "Redirect to the Request Reset Password Url. Validates cip_authdata cookie.")
    @ApiResponses({
            @ApiResponse(code = 302, message = "RP config found, will set cookie data and redirect to Registration page."),
            @ApiResponse(code = 400, message = "Bad request, missing or invalid params."),
            @ApiResponse(code = 404, message = "clientId not found."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "client_id", value = "RP Client ID", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "redirect_uri", value = "URL to redirect", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "state", value = "State", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "response_type", value = "Response Type", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "scope", value = "Scope", required = true, dataType = "String", paramType = "query")
    })
    public ResponseEntity<?> getRPResetPasswordConfig(
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam("response_type") String responseType,
            @RequestParam("scope") String scope
    ) throws UnsupportedEncodingException {
        if (!Utils.isNullOrEmpty(state)) {
            state = encodeService.decodeUTF8(state);
        }

        CIPAuthDataDTO cipAuthData = CIPAuthDataDTO.builder()
                .clientId(clientId)
                .redirectUri(redirectUri)
                .state(state)
                .responseType(responseType)
                .scope(scope)
                .build();

        logger.info("Get RP Process started");
        if (cipAuthData.areClientIdAndRedirectUriInvalid()) {
            logger.error("Either clientId or redirectURI missing");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            boolean uriExists = false;
            logger.info("Getting RP data");
            OpenIdRelyingParty openIdRelyingParty = cdcResponseHandler.getRP(cipAuthData.getClientId());
            for (String uri : openIdRelyingParty.getRedirectUris()) {
                logger.info(String.format("Find %s in OpenId redirectURIs", cipAuthData.getRedirectUri()));
                if (uri.equalsIgnoreCase(cipAuthData.getRedirectUri())) {
                    uriExists = true;
                    logger.info(String.format("%s was found", cipAuthData.getRedirectUri()));
                    break;
                }
            }

            if (!uriExists) {
                String error = String.format("%s was not found in RP URIs", cipAuthData.getRedirectUri());
                logger.error(error);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(REQUEST_EXCEPTION_HEADER, error).build();
            }

            logger.info("Building cip_authdata cookie to get login endpoint.");
            String cipAuthDataCookie = cookieService.createCIPAuthDataCookie(cipAuthData, getOidcLoginEndpointPath);
            logger.info("cip_authdata cookie built.");

            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header(HttpHeaders.SET_COOKIE, cipAuthDataCookie)
                    .header(HttpHeaders.LOCATION, rpRedirectUri)
                    .build();
        }
        catch (CustomGigyaErrorException customGigyaException) {
            if (customGigyaException.getMessage().contains("404000")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(REQUEST_EXCEPTION_HEADER, customGigyaException.getMessage()).body(null);
        } catch (GSKeyNotFoundException gsKeyNotFoundException) {
            logger.error(String.format("GSKeyNotFoundException: %s", gsKeyNotFoundException.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private ResetPasswordResponse createResetPasswordResponse(int responseCode,String responseMessage) {
        return ResetPasswordResponse.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    private void sendRequestResetPasswordEmail(String username, RequestResetPasswordDTO requestResetPasswordDTO) throws IOException, CustomGigyaErrorException {
        logger.info("Preparing request reset password confirmation email");
        String email = cdcResponseHandler.getEmailByUsername(username);
        AccountInfo account = cdcResponseHandler.getAccountInfoByEmail(email);
        notificationService.sendRequestResetPasswordEmailNotification(account, requestResetPasswordDTO);
        logger.info("Request reset password email sent");
    }
}
