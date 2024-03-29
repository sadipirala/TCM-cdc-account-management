package com.thermofisher.cdcam.controller;

import com.gigya.socialize.GSKeyNotFoundException;
import com.thermofisher.cdcam.enums.CookieType;
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
import com.thermofisher.cdcam.services.CookieService;
import com.thermofisher.cdcam.services.EncodeService;
import com.thermofisher.cdcam.services.GigyaService;
import com.thermofisher.cdcam.services.JWTService;
import com.thermofisher.cdcam.services.LoginService;
import com.thermofisher.cdcam.services.NotificationService;
import com.thermofisher.cdcam.services.ReCaptchaService;
import com.thermofisher.cdcam.services.SecretsService;
import com.thermofisher.cdcam.services.URLService;
import com.thermofisher.cdcam.services.hashing.HashingService;
import com.thermofisher.cdcam.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Set;


@RestController
@Slf4j
@RequestMapping("/identity/reset-password")
public class ResetPasswordController {

    private final String REQUEST_EXCEPTION_HEADER = "Request-Exception";

    @Value("${identity.reset-password.get-login-endpoint.path}")
    private String getOidcLoginEndpointPath;

    @Value("${identity.reset-password.redirect_uri}")
    private String rpRedirectUri;

    @Autowired
    CookieService cookieService;

    @Autowired
    EncodeService encodeService;

    @Autowired
    GigyaService gigyaService;

    @Autowired
    LoginService loginService;

    @Autowired
    JWTService jwtService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    ReCaptchaService reCaptchaService;

    @Autowired
    SecretsService secretsService;

    @Autowired
    URLService urlService;

    @PostMapping("/email")
    @Operation(description = "sends the request to reset a password.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<?> sendResetPasswordEmail(
            @RequestBody ResetPasswordRequest body,
            @CookieValue(name = "cip_authdata", required = false) String cipAuthData,
            @RequestHeader(name = ReCaptchaService.CAPTCHA_TOKEN_HEADER, required = false) String captchaValidationToken
    ) {
        log.info(String.format("Requested reset password for user: %s", body.getUsername()));
        if (Utils.isNullOrEmpty(cipAuthData)) {
            log.info("Cookie not present.");
            cipAuthData = cookieService.buildDefaultCipAuthDataCookie(CookieType.RESET_PASSWORD);
        }
        try {
            JSONObject reCaptchaResponse = reCaptchaService.verifyToken(body.getCaptchaToken(), captchaValidationToken);
            log.info(String.format("reCaptcha response for %s: %s", body.getUsername(), reCaptchaResponse.toString()));
            String passwordToken = gigyaService.resetPasswordRequest(body.getUsername());
            log.info(String.format("Request reset password was successfully for: %s", body.getUsername()));
            RequestResetPasswordDTO requestResetPasswordDTO = RequestResetPasswordDTO.builder()
                    .passwordToken(passwordToken)
                    .authData(cipAuthData)
                    .build();

            sendRequestResetPasswordEmail(body.getUsername(), requestResetPasswordDTO);

            log.info(String.format("Request for reset password successful for: %s", body.getUsername()));
            return ResponseEntity.ok().build();
        } catch (LoginIdDoesNotExistException e) {
            log.info(e.getMessage());
            return ResponseEntity.ok().build();
        } catch (CustomGigyaErrorException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (ReCaptchaLowScoreException v3Exception) {
            String jwtToken = jwtService.create();
            log.error(String.format("reCaptcha v3 error for: %s. message: %s", body.getUsername(), v3Exception.getMessage()));
            return ResponseEntity.accepted().header(ReCaptchaService.CAPTCHA_TOKEN_HEADER, jwtToken).build();
        } catch (ReCaptchaUnsuccessfulResponseException v2Exception) {
            log.error(String.format("reCaptcha v2 error for: %s. message: %s", body.getUsername(), v2Exception.getMessage()));
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error(String.format("Error: %s", e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PutMapping("/")
    @Operation(description = "resets the password.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody ResetPasswordSubmit body) {
        log.info(String.format("Reset password process started for: %s", body.getUid()));
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ResetPasswordSubmit>> violations = validator.validate(body);
        final int EXPIRED_TOKEN_ERROR = 403025;

        if (violations.size() > 0) {
            log.error(String.format("One or more errors occurred while creating the reset password object. %s", violations.toArray()));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            ResetPasswordResponse response = gigyaService.resetPasswordSubmit(body);

            if (response.getResponseCode() != 0) {
                if (response.getResponseCode() == EXPIRED_TOKEN_ERROR) {
                    HttpHeaders responseHeaders = new HttpHeaders();
                    responseHeaders.set("redirect", "/expired");
                    log.warn(String.format("Expired token, user UID: %s. message: %s", body.getUid(), response.getResponseMessage()));
                    return new ResponseEntity<>(response, responseHeaders, HttpStatus.FOUND);
                } else {
                    log.warn(String.format("Failed to reset password for user with UID: %s. message: %s", body.getUid(), response.getResponseMessage()));
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }

            try {
                gigyaService.updateRequirePasswordCheck(body.getUid());
            } catch (CustomGigyaErrorException e) {
                log.error(e.getMessage());
            }

            log.info(String.format("Build password update notification started for: %s", body.getUid()));
            String hashedPassword = HashingService.toMD5(body.getNewPassword());
            PasswordUpdateNotification passwordUpdateNotification = PasswordUpdateNotification.builder()
                    .newPassword(hashedPassword)
                    .uid(body.getUid())
                    .build();
            notificationService.sendPasswordUpdateNotification(passwordUpdateNotification);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error(String.format("An exception occurred: %s", e.getMessage()));
            ResetPasswordResponse response = createResetPasswordResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = {"/oidc/rp", "/rp"})
    @Operation(description = "Redirect to the Request Reset Password Url. Validates cip_authdata cookie.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "RP config found, will set cookie data and redirect to Registration page."),
            @ApiResponse(responseCode = "400", description = "Bad request, missing or invalid params."),
            @ApiResponse(responseCode = "404", description = "clientId not found."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @Parameters({
            @Parameter(name = "client_id", description = "RP Client ID", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "redirect_uri", description = "URL to redirect", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "state", description = "State", schema = @Schema(type = "string"), in = ParameterIn.QUERY, required = false),
            @Parameter(name = "response_type", description = "Response Type", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "scope", description = "Scope", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY)
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

        log.info("Get RP Process started");
        if (cipAuthData.areClientIdAndRedirectUriInvalid()) {
            log.error("Either clientId or redirectURI missing");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            boolean uriExists = false;
            log.info("Getting RP data");
            OpenIdRelyingParty openIdRelyingParty = gigyaService.getRP(cipAuthData.getClientId());
            for (String uri : openIdRelyingParty.getRedirectUris()) {
                log.info(String.format("Find %s in OpenId redirectURIs", cipAuthData.getRedirectUri()));
                if (uri.equalsIgnoreCase(cipAuthData.getRedirectUri())) {
                    uriExists = true;
                    log.info(String.format("%s was found", cipAuthData.getRedirectUri()));
                    break;
                }
            }

            if (!uriExists) {
                String error = String.format("%s was not found in RP URIs", cipAuthData.getRedirectUri());
                log.error(error);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(REQUEST_EXCEPTION_HEADER, error).build();
            }

            log.info("Building cip_authdata cookie to get login endpoint.");
            String cipAuthDataCookie = cookieService.createCIPAuthDataCookie(cipAuthData, getOidcLoginEndpointPath);
            log.info("cip_authdata cookie built.");

            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header(HttpHeaders.SET_COOKIE, cipAuthDataCookie)
                    .header(HttpHeaders.LOCATION, rpRedirectUri)
                    .build();
        } catch (CustomGigyaErrorException customGigyaException) {
            if (customGigyaException.getMessage().contains("404000")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(REQUEST_EXCEPTION_HEADER, customGigyaException.getMessage()).body(null);
        } catch (GSKeyNotFoundException gsKeyNotFoundException) {
            log.error(String.format("GSKeyNotFoundException: %s", gsKeyNotFoundException.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private ResetPasswordResponse createResetPasswordResponse(int responseCode, String responseMessage) {
        return ResetPasswordResponse.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    private void sendRequestResetPasswordEmail(String username, RequestResetPasswordDTO requestResetPasswordDTO) throws IOException, CustomGigyaErrorException {
        log.info("Preparing request reset password confirmation email");
        String email = gigyaService.getEmailByUsername(username);
        AccountInfo account = gigyaService.getAccountInfoByEmail(email);
        notificationService.sendRequestResetPasswordEmailNotification(account, requestResetPasswordDTO);
        log.info("Request reset password email sent");
    }
}
