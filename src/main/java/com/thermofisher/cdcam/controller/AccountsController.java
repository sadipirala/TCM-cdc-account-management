package com.thermofisher.cdcam.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotBlank;

import com.thermofisher.cdcam.model.*;
import com.thermofisher.cdcam.model.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.enums.InviteSource;
import com.thermofisher.cdcam.enums.RegistrationType;
import com.thermofisher.cdcam.enums.ResponseCode;
import com.thermofisher.cdcam.enums.cdc.GigyaCodes;
import com.thermofisher.cdcam.enums.cdc.WebhookEvent;
import com.thermofisher.cdcam.model.cdc.CDCResponse;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.JWTPublicKey;
import com.thermofisher.cdcam.model.notifications.AccountUpdatedNotification;
import com.thermofisher.cdcam.model.notifications.PasswordUpdateNotification;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaLowScoreException;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaUnsuccessfulResponseException;
import com.thermofisher.cdcam.services.AccountsService;
import com.thermofisher.cdcam.services.CookieService;
import com.thermofisher.cdcam.services.DataProtectionService;
import com.thermofisher.cdcam.services.EmailVerificationService;
import com.thermofisher.cdcam.services.GigyaService;
import com.thermofisher.cdcam.services.InvitationService;
import com.thermofisher.cdcam.services.JWTService;
import com.thermofisher.cdcam.services.JWTValidator;
import com.thermofisher.cdcam.services.NotificationService;
import com.thermofisher.cdcam.services.ReCaptchaService;
import com.thermofisher.cdcam.services.SecretsService;
import com.thermofisher.cdcam.services.UpdateAccountService;
import com.thermofisher.cdcam.services.hashing.HashingService;
import com.thermofisher.cdcam.utils.PasswordUtils;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.UsersHandler;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/accounts")
public class AccountsController {
    private Logger logger = LogManager.getLogger(this.getClass());

    @Value("${general.cipdc}")
    private String cipdc;

    @Value("${identity.oidc.rp.id}")
    private String defaultClientId;

    @Value("${is-registration-notification-enabled}")
    private boolean isRegistrationNotificationEnabled;

    @Autowired
    AccountsService accountsService;

    @Autowired
    EmailVerificationService emailVerificationService;

    @Autowired
    GigyaService gigyaService;

    @Autowired
    CookieService cookieService;

    @Autowired
    DataProtectionService dataProtectionService;

    @Autowired
    InvitationService invitationService;

    @Autowired
    JWTService jwtService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    ReCaptchaService reCaptchaService;

    @Autowired
    SecretsService secretsService;

    @Autowired
    UpdateAccountService updateAccountService;

    @Autowired
    UsersHandler usersHandler;

    @PutMapping("/{uid}/password")
    @ApiOperation(value = "Updates user's password.")
    @ApiResponses({
        @ApiResponse(code = 204, message = "Password updated successfully."),
        @ApiResponse(code = 400, message = "Either the new password does not comply with the password requirements or the old password is incorrect."),
        @ApiResponse(code = 500, message = "Internal server error.")
    })
    @ApiImplicitParam(name = "uid", value = "To be updated user's UID.", required = true)
    public ResponseEntity<?> changePassword(@Valid @PathVariable String uid, @Valid @RequestBody ChangePasswordDTO body) {
        logger.info(String.format("Attempting password change for %s", uid));
        try {
            if (!PasswordUtils.isPasswordValid(body.getNewPassword())) {
                throw new IllegalArgumentException("Invalid password. Bad request.");
            }
            gigyaService.changePassword(uid, body.getNewPassword(), body.getPassword());
            logger.info(String.format("Password updated successfully for %s. Sending SNS notification.", uid));
            String hashedPassword = HashingService.toMD5(body.getNewPassword());
            PasswordUpdateNotification passwordUpdateNotification = PasswordUpdateNotification.builder().uid(uid).newPassword(hashedPassword).build();
            notificationService.sendPasswordUpdateNotification(passwordUpdateNotification);
            logger.info("Update password SNS notification sent.");
            return ResponseEntity.noContent().build();
        } catch (CustomGigyaErrorException | IllegalArgumentException e) {
            logger.info(String.format("Error during password update for %s. %s", uid, e.getMessage()));
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.info(String.format("Error during password update for %s. %s", uid, e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/users/{uids}")
    @ApiOperation(value = "Gets a list of users.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    @ApiImplicitParam(name = "uids", value = "Comma-separated list of CDC UIDs", required = true)
    public ResponseEntity<List<UserDetails>> getUsers(@PathVariable List<String> uids) {
        try {
            logger.info("User data by UID requested.");
            List<UserDetails> userDetails = usersHandler.getUsers(uids);
            
            logger.info(String.format("Retrieved %d user(s)", userDetails.size()));
            return (userDetails.size() > 0) ? new ResponseEntity<>(userDetails, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            logger.error(String.format("An error occurred while retrieving user data from CDC: %s", Utils.stackTraceToString(e)));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{uid}")
    @ApiOperation(value = "Get user profile by UID")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Account Not found"),
        @ApiResponse(code = 500, message = "Internal server error.")
    })
    @ApiImplicitParam(name = "uid", value = "A valid UID", required = true)
    public ResponseEntity<ProfileInfoDTO> getUserProfileByUID(@PathVariable String uid) {
        try {
            logger.info("User profile data by UID requested.");
            ProfileInfoDTO profileInfoDTO = usersHandler.getUserProfileByUID(uid);
            logger.info(String.format("Retrieved user with UID: %s", uid));
            return (profileInfoDTO != null) ? new ResponseEntity<>(profileInfoDTO, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            logger.error(String.format("An error occurred while retrieving user data from CDC: %s", Utils.stackTraceToString(e)));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/user")
    @ApiOperation(value = "Notifies a user successful registration in CDC to the subscribed entities.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<String> onAccountRegistered(@RequestHeader("x-gigya-sig-jwt") String jwt, @RequestBody String body) {
        logger.info(String.format("/accounts/user called with body: %s", body));
        
        try {
            logger.info("JWT validation started.");
            logger.info("Getting JWT public key.");
            JWTPublicKey jwtPublicKey = gigyaService.getJWTPublicKey();
            logger.info("Validating JWT signature.");
            boolean isJWTValid = JWTValidator.isValidSignature(jwt, jwtPublicKey);

            if (!isJWTValid) {
                logger.info("Invalid JWT signature. Bad request.");
                return new ResponseEntity<>(HttpStatus.OK);
            }
            logger.info("JWT signature valid.");
            JSONObject jsonBody = new JSONObject(body);
            JSONArray events = (JSONArray) jsonBody.get("events");
            if (events.length() == 0) {
                logger.error("No webhook events found in request.");
            }

            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);
                if (!event.get("type").equals(WebhookEvent.REGISTRATION.getValue())) continue;

                JSONObject data = (JSONObject) event.get("data");
                String uid = data.get("uid").toString();
                accountsService.onAccountRegistered(uid);
            }
        } catch (Exception e) {
            logger.error(String.format("onAccountRegistered error: %s. %s.", e.getMessage(), e.getCause()));
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping()
    @ApiOperation(value = "Inserts a new Account")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<CDCResponseData> newAccount(
        @RequestBody @Valid AccountInfoDTO accountInfoDTO, 
        @CookieValue(name = "cip_authdata", required = false) String cipAuthDataCookieString,
        @RequestHeader(name = ReCaptchaService.CAPTCHA_TOKEN_HEADER, required = false) String captchaValidationToken
    ) throws IOException, JSONException {
        logger.info(String.format("Account registration initiated. Username: %s", accountInfoDTO.getUsername()));
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<AccountInfoDTO>> violations = validator.validate(accountInfoDTO);

        if (violations.size() > 0) {
            logger.error(String.format("One or more errors occurred while creating the account. %s", violations.toArray()));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!Utils.isValidEmail(accountInfoDTO.getEmailAddress())) {
            logger.error(String.format("Email is invalid. %s", accountInfoDTO.getEmailAddress()));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        try {
            JSONObject reCaptchaResponse = reCaptchaService.verifyToken(accountInfoDTO.getReCaptchaToken(), captchaValidationToken);
            logger.info(String.format("reCaptcha response for %s: %s", accountInfoDTO.getUsername(), reCaptchaResponse.toString()));
        } catch (ReCaptchaLowScoreException v3Exception) {
            String jwtToken = jwtService.create();
            logger.error(String.format("reCaptcha v3 error for: %s. message: %s", accountInfoDTO.getUsername(), v3Exception.getMessage()));
            return ResponseEntity.accepted().header(ReCaptchaService.CAPTCHA_TOKEN_HEADER, jwtToken).build();
        } catch (ReCaptchaUnsuccessfulResponseException v2Exception) {
            logger.error(String.format("reCaptcha v2 error for: %s. message: %s", accountInfoDTO.getUsername(), v2Exception.getMessage()));
            return ResponseEntity.badRequest().build();
        } catch (JSONException jsonException) {
            logger.error(String.format("JSONException while verifying reCaptcha token: %s.", jsonException.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        String ciphertext = accountInfoDTO.getCiphertext();
        Ciphertext decryptedCiphertext = null;
        if (StringUtils.isNotBlank(ciphertext)) {
            decryptedCiphertext = dataProtectionService.decrypCiphertext(ciphertext);
            accountInfoDTO.setCiphertextData(decryptedCiphertext);
        }

        AccountInfo account = AccountBuilder.buildFrom(accountInfoDTO);
        CIPAuthDataDTO cipAuthDataDTO;
        if (StringUtils.isNotBlank(cipAuthDataCookieString)) {
            logger.info("Decoding cip_authdata.");
            cipAuthDataDTO = cookieService.decodeCIPAuthDataCookie(cipAuthDataCookieString);
            account.setOpenIdProviderId(cipAuthDataDTO.getClientId());
            logger.info("cip_authdata decoded. Provider clientId set.");
        } else {
            account.setOpenIdProviderId(defaultClientId);
            logger.info("Blank cip_authdata. Setting default Provider ClientId.");
        }

        try {
            logger.info(String.format("Creating new account for %s", account.getUsername()));
            CDCResponseData accountCreationResponse = accountsService.createAccount(account);
            String newAccountUid = accountCreationResponse.getUID();
            boolean isVerificationPending = EmailVerificationService.isVerificationPending(accountCreationResponse);
            account.setUid(newAccountUid);
            logger.info(String.format("Account registration successful. Username: %s. UID: %s", account.getUsername(), newAccountUid));
            
            // TODO: Move into a service. Or simply create a model for this notification?
            String hashedPassword = HashingService.toMD5(account.getPassword());
            account.setPassword(hashedPassword);
            
            if (isRegistrationNotificationEnabled) {
                logger.info(String.format("Sending account registered notification for UID: %s", newAccountUid));
                notificationService.sendAccountRegisteredNotification(account, cipdc);
                logger.info(String.format("Account registered notification sent for UID: %s", newAccountUid));
            }

            logger.info(String.format("Sending account info notification for UID: %s", newAccountUid));
            notificationService.sendNotifyAccountInfoNotification(account, cipdc);
            logger.info(String.format("Account info notification sent for UID: %s", newAccountUid));

            // TODO: Check notifications possible error scenarios
            if (isAspireRegistrationValid(account)) {
                logger.info(String.format("Sending Aspire registration notification for UID: %s", newAccountUid));
                notificationService.sendAspireRegistrationNotification(account);
                logger.info(String.format("Aspire registration notification sent for UID: %s", newAccountUid));
            }

            if (account.getRegistrationType() != null && account.getRegistrationType().equals(RegistrationType.BASIC.getValue())) {
                logger.info(String.format("Sending registration confirmation notification for UID: %s", newAccountUid));
                notificationService.sendConfirmationEmailNotification(account);
                logger.info(String.format("Registration confirmation notification sent for UID: %s", newAccountUid));
            }

            if (accountCreationResponse.getErrorCode() == GigyaCodes.SUCCESS.getValue()) {
                logger.info(String.format("Sending email verification notification for UID: %s", newAccountUid));
                emailVerificationService.sendVerificationByLinkEmail(newAccountUid);
                logger.info(String.format("Email verification notification for UID: %s", newAccountUid));
            }

            if(isInvitedAccount(decryptedCiphertext)) {
                logger.info("Updating invitation with country code.");
                JSONObject updateInvitationDTO = new JSONObject() 
                    .put("inviteeUsername", account.getUsername())
                    .put("country", account.getCountry());
                Integer response = invitationService.updateInvitationCountry(updateInvitationDTO);
                if(response == HttpStatus.OK.value()) {
                    logger.info("Invitation was updated successfully with country value.");
                } else {
                    logger.info("An error occurred. Invitation was not updated with country value.");
                }
            }

            if (isVerificationPending && isInvitedAccount(decryptedCiphertext)) {
                logger.info(String.format("Verifying email for invited user: %s", newAccountUid));
                CDCResponse verifyResponse = accountsService.verify(account, accountCreationResponse.getRegToken());
                accountCreationResponse.setErrorCode(verifyResponse.getErrorCode());
                accountCreationResponse.setStatusCode(verifyResponse.getStatusCode());
                accountCreationResponse.setStatusReason(verifyResponse.getStatusReason());
                logger.info(String.format("Auto email verification successful for user: %s", newAccountUid));
            } else if (isVerificationPending) {
                logger.info("Email verification pending. Returning response with error {}.", ResponseCode.ACCOUNT_PENDING_VERIFICATION.getValue());
                accountCreationResponse.setErrorCode(ResponseCode.ACCOUNT_PENDING_VERIFICATION.getValue());
            }

            return new ResponseEntity<>(accountCreationResponse, HttpStatus.OK);
        } catch (CustomGigyaErrorException e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
        } catch (Exception e) {
            logger.error(String.format("An error occurred while creating account for: %s , Error: %s", account.getUsername(), Utils.stackTraceToString(e)));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isInvitedAccount(Ciphertext ciphertext) {
        return ciphertext != null && InviteSource.contains(ciphertext.getSource());
    }

    private boolean isAspireRegistrationValid (AccountInfo accountInfoDTO) {
        return accountInfoDTO.getAcceptsAspireEnrollmentConsent() != null && accountInfoDTO.getAcceptsAspireEnrollmentConsent()
            && accountInfoDTO.getIsHealthcareProfessional() != null && !accountInfoDTO.getIsHealthcareProfessional()
            && accountInfoDTO.getAcceptsAspireTermsAndConditions() != null && accountInfoDTO.getAcceptsAspireTermsAndConditions();
    }

    @PostMapping("/merged")
    @ApiOperation(value = "Updates account data based on the accountMerged event from CDC.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<String> onAccountsMerge(@RequestHeader("x-gigya-sig-jwt") String jwt, @RequestBody String body) {
        logger.info(String.format("/accounts/merged called with body: %s", body));
        try {
            logger.info("JWT validation started.");
            logger.info("Getting JWT public key.");
            JWTPublicKey jwtPublicKey = gigyaService.getJWTPublicKey();
            logger.info("Validating JWT signature.");
            boolean isJWTValid = JWTValidator.isValidSignature(jwt, jwtPublicKey);
    
            if (!isJWTValid) {
                logger.info("Invalid JWT signature. Bad request.");
                return new ResponseEntity<>(HttpStatus.OK);
            }
            logger.info("JWT signature valid.");
            
            JSONObject jsonBody = new JSONObject(body);
            JSONArray events = (JSONArray) jsonBody.get("events");

            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);
     
                if (isMergeEvent(event)) {
                    logger.info("accountMerged webhook event fired.");
                    JSONObject data = (JSONObject) event.get("data");
                    String uid = data.get("newUid").toString();
                    accountsService.onAccountMerged(uid);
                } else if (isUpdateEvent(event)) {
                    JSONObject data = (JSONObject) event.get("data");
                    logger.info("accountUpdate webhook event fired.");
                    String uid = data.get("uid").toString();
                    accountsService.onAccountUpdated(uid);
                }
            }
        } catch (Exception e) {
            logger.error(String.format("onAccountsMerge error: %s. %s.", e.getMessage(), e.getCause()));
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private boolean isMergeEvent(JSONObject event) throws JSONException {
        return event.get("type").equals(WebhookEvent.MERGE.getValue());
    }

    private boolean isUpdateEvent(JSONObject event) throws JSONException {
        return event.get("type").equals(WebhookEvent.UPDATE.getValue());
    }

    @PostMapping("/username/recovery")
    @ApiOperation(value = "Sends Username Recovery email.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<String> sendRecoverUsernameEmail(@RequestBody @Valid UsernameRecoveryDTO usernameRecoveryDTO) {
        try {
            String email = usernameRecoveryDTO.getUserInfo().getEmail();
            logger.info(String.format("Username recovery email requested by %s", email));
            AccountInfo account = gigyaService.getAccountInfoByEmail(email);

            if (account == null) {
                logger.warn(String.format("No account found for %s while sending username recovery email.", email));
                return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
            }

            logger.info(String.format("Sending username recovery email to %s", email));
            notificationService.sendRecoveryUsernameEmailNotification(usernameRecoveryDTO, account);
            logger.info(String.format("Username recovery email sent to %s", email));

            return new ResponseEntity<>("OK", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/user/verification-email/{uid}")
    @ApiOperation(value = "Triggers CDC email verification process.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    @ApiImplicitParam(name = "uid", value = "CDC user UID.", required = true, dataType = "string")
    public ResponseEntity<CDCResponseData> sendVerificationEmail(@PathVariable String uid) {
        logger.info(String.format("CDC verification email process triggered for user with UID: %s", uid));

        CDCResponseData responseData = emailVerificationService.sendVerificationByLinkEmailSync(uid);
        HttpStatus responseStatus = HttpStatus.valueOf(responseData.getStatusCode());

        return new ResponseEntity<>(responseData, responseStatus);
    }

    @PutMapping("/user/timezone")
    @ApiOperation(value = "Sets the user's Timezone in CDC.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<String> setTimezone(@RequestBody @Valid UserTimezone userTimezone) throws JSONException, JsonProcessingException {
        HttpStatus updateUserTimezoneStatus = updateAccountService.updateTimezoneInCDC(userTimezone.getUid(), userTimezone.getTimezone());
        if (updateUserTimezoneStatus == HttpStatus.OK) {
            String message = String.format("User %s updated.", userTimezone.getUid());
            logger.info(message);
            return new ResponseEntity<>(message, updateUserTimezoneStatus);
        } else {
            String message = String.format("An error occurred during the user's timezone update. UID: %s", userTimezone.getUid());
            logger.error(message);
            return new ResponseEntity<>(message, updateUserTimezoneStatus);
        }
    }

    @GetMapping("/{loginID}")
    @ApiOperation(value = "Checks whether a loginID is available in CDC.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK."),
            @ApiResponse(code = 404, message = "Not found."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    @ApiImplicitParam(name = "loginID", value = "LoginID to be checked in CDC.", required = true)
    public ResponseEntity<AccountAvailabilityResponse> isAvailableLoginID(@PathVariable @NotBlank String loginID) {
        logger.info(String.format("Check for loginID availability started. Login ID: %s", loginID));

        Boolean cdcAvailabilityResponse;
        try {
            cdcAvailabilityResponse = gigyaService.isAvailableLoginId(loginID);
        } catch (Exception e) {
            String exception = Utils.stackTraceToString(e);
            logger.error(String.format("An error occurred while checking availability in CDC for: %s. Error: %s", loginID, exception));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
        }

        logger.info(String.format("Is %s available in CDC: %b", loginID, cdcAvailabilityResponse));

        AccountAvailabilityResponse response = AccountAvailabilityResponse.builder()
                .isCDCAvailable(cdcAvailabilityResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/user")
    @ApiOperation(value = "Update User Profile in CDC.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK."),
            @ApiResponse(code = 400, message = "Bad Request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<String> updateUserProfile(@RequestBody ProfileInfoDTO profileInfoDTO) throws CustomGigyaErrorException {
        try {
            if (profileInfoDTO == null || Utils.isNullOrEmpty(profileInfoDTO.getUid())) {
                logger.error("ProfileInfoDTO is null or UID is null/empty");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            
            logger.info(String.format("Starting user profile update process with UID: %s", profileInfoDTO.getUid()));
            String uid = profileInfoDTO.getUid();
            AccountInfo accountInfoDTO = gigyaService.getAccountInfo(uid);
            profileInfoDTO.setActualEmail(accountInfoDTO.getEmailAddress());
            profileInfoDTO.setActualUsername(accountInfoDTO.getUsername());
            HttpStatus updateUserProfileStatus = updateAccountService.updateProfile(profileInfoDTO);
            if (updateUserProfileStatus == HttpStatus.OK) {
                logger.info(String.format("User %s updated.", uid));

                AccountInfo updatedAccountInfo = gigyaService.getAccountInfo(uid);
                logger.info("Building AccountUpdatedNotification object.");
                AccountUpdatedNotification accountUpdatedNotification = AccountUpdatedNotification.build(updatedAccountInfo);
                logger.info("Sending accountUpdated notification.");
                notificationService.sendPrivateAccountUpdatedNotification(accountUpdatedNotification);
                notificationService.sendPublicAccountUpdatedNotification(accountUpdatedNotification);
                logger.info("accountUpdated notification sent.");
                return new ResponseEntity<String>("The information was successfully updated.", updateUserProfileStatus);
            } else {
                logger.error(String.format("An error occurred during the user's profile update. UID: %s", uid));
                return new ResponseEntity<>(updateUserProfileStatus);
            }
        } catch (JSONException ex) {
            logger.error(String.format("Internal server error : %s", ex.getMessage()));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/user/consent")
    @ApiOperation(value = "Update user marketing consent preferences")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK."),
            @ApiResponse(code = 400, message = "Bad Request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<String> updatedMarketingConsent(@RequestBody @Valid SelfServeConsentDTO selfServeConsentDTO) {
        try {
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error(String.format("An unexpected exception occurred: %s", e.getMessage()));
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public String handleHttpMessageNotReadableExceptions(
            HttpMessageNotReadableException ex) {
        return "Invalid input format. Message not readable.";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = JsonProcessingException.class)
    public String handleHttpMessageNotReadableExceptions(
            JsonProcessingException ex) {
        return "Invalid input format. Message not readable.";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = ParseException.class)
    public String handleHttpMessageNotReadableExceptions(
            ParseException ex) {
        return "Invalid input format. Message not readable.";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public InvalidArgumentExceptionCustomResponse handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        List<ObjectError> err = ex.getBindingResult().getAllErrors();
        List<String> errorMessages = err.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());

        return InvalidArgumentExceptionCustomResponse.builder().errors(errorMessages).build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = NullPointerException.class)
    public String handleNullPointerException(
            NullPointerException ex) {
        return "Invalid input. Null body present.";
    }
}
