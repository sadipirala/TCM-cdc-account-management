package com.thermofisher.cdcam.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.enums.InviteSource;
import com.thermofisher.cdcam.enums.RegistrationType;
import com.thermofisher.cdcam.enums.ResponseCode;
import com.thermofisher.cdcam.enums.cdc.GigyaCodes;
import com.thermofisher.cdcam.enums.cdc.WebhookEvent;
import com.thermofisher.cdcam.model.AccountAvailabilityResponse;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.Ciphertext;
import com.thermofisher.cdcam.model.InvalidArgumentExceptionCustomResponse;
import com.thermofisher.cdcam.model.UserDetails;
import com.thermofisher.cdcam.model.UserTimezone;
import com.thermofisher.cdcam.model.cdc.CDCResponse;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.JWTPublicKey;
import com.thermofisher.cdcam.model.dto.AccountInfoDTO;
import com.thermofisher.cdcam.model.dto.CIPAuthDataDTO;
import com.thermofisher.cdcam.model.dto.ChangePasswordDTO;
import com.thermofisher.cdcam.model.dto.ConsentDTO;
import com.thermofisher.cdcam.model.dto.ProfileInfoDTO;
import com.thermofisher.cdcam.model.dto.UsernameRecoveryDTO;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
@RestController
@RequestMapping("/accounts")

public class AccountsController {

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
    @Operation(description = "Updates user's password.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password updated successfully."),
            @ApiResponse(responseCode = "400", description = "Either the new password does not comply with the password requirements or the old password is incorrect."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @Parameter(name = "uid", description = "To be updated user's UID.", required = true)
    public ResponseEntity<?> changePassword(@Valid @PathVariable String uid, @Valid @RequestBody ChangePasswordDTO body) {
        log.info(String.format("Attempting password change for %s", uid));
        try {
            if (!PasswordUtils.isPasswordValid(body.getNewPassword())) {
                throw new IllegalArgumentException("Invalid password. Bad request.");
            }
            gigyaService.changePassword(uid, body.getNewPassword(), body.getPassword());
            log.info(String.format("Password updated successfully for %s. Sending SNS notification.", uid));
            String hashedPassword = HashingService.toMD5(body.getNewPassword());
            PasswordUpdateNotification passwordUpdateNotification = PasswordUpdateNotification.builder().uid(uid).newPassword(hashedPassword).build();
            notificationService.sendPasswordUpdateNotification(passwordUpdateNotification);
            log.info("Update password SNS notification sent.");
            return ResponseEntity.noContent().build();
        } catch (CustomGigyaErrorException | IllegalArgumentException e) {
            log.info(String.format("Error during password update for %s. %s", uid, e.getMessage()));
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.info(String.format("Error during password update for %s. %s", uid, e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/users/{uids}")
    @Operation(description = "Gets a list of users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @Parameter(name = "uids", description = "Comma-separated list of CDC UIDs", required = true)
    public ResponseEntity<List<UserDetails>> getUsers(@PathVariable List<String> uids) {
        try {
            log.info("User data by UID requested.");
            List<UserDetails> userDetails = usersHandler.getUsers(uids);

            log.info(String.format("Retrieved %d user(s)", userDetails.size()));
            return (userDetails.size() > 0) ? new ResponseEntity<>(userDetails, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            log.error(String.format("An error occurred while retrieving user data from CDC: %s", Utils.stackTraceToString(e)));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{uid}")
    @Operation(description = "Get user profile by UID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Account Not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @Parameter(name = "uid", description = "A valid UID", required = true)
    public ResponseEntity<ProfileInfoDTO> getUserProfileByUID(@PathVariable String uid) {
        try {
            log.info("User profile data by UID requested.");
            ProfileInfoDTO profileInfoDTO = usersHandler.getUserProfileByUID(uid);
            log.info(String.format("Retrieved user with UID: %s", uid));
            return (profileInfoDTO != null) ? new ResponseEntity<>(profileInfoDTO, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            log.error(String.format("An error occurred while retrieving user data from CDC: %s", Utils.stackTraceToString(e)));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/user")
    @Operation(description = "Notifies a user successful registration in CDC to the subscribed entities.")
    @ApiResponses(value={
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<String> onAccountRegistered(@RequestHeader("x-gigya-sig-jwt") String jwt, @RequestBody String body) {
        log.info(String.format("/accounts/user called with body: %s", body));

        try {
            log.info("JWT validation started.");
            log.info("Getting JWT public key.");
            JWTPublicKey jwtPublicKey = gigyaService.getJWTPublicKey();
            log.info("Validating JWT signature.");
            boolean isJWTValid = JWTValidator.isValidSignature(jwt, jwtPublicKey);

            if (!isJWTValid) {
                log.info("Invalid JWT signature. Bad request.");
                return new ResponseEntity<>(HttpStatus.OK);
            }
            log.info("JWT signature valid.");
            JSONObject jsonBody = new JSONObject(body);
            JSONArray events = (JSONArray) jsonBody.get("events");
            if (events.length() == 0) {
                log.error("No webhook events found in request.");
            }

            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);
                if (!event.get("type").equals(WebhookEvent.REGISTRATION.getValue())) continue;

                JSONObject data = (JSONObject) event.get("data");
                String uid = data.get("uid").toString();
                accountsService.onAccountRegistered(uid);
            }
        } catch (Exception e) {
            log.error(String.format("onAccountRegistered error: %s. %s.", e.getMessage(), e.getCause()));
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping()
    @Operation(description = "Inserts a new Account")
    @ApiResponses(value={
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<CDCResponseData> newAccount(
            @RequestBody @Valid AccountInfoDTO accountInfoDTO,
            @CookieValue(name = "cip_authdata", required = false) String cipAuthDataCookieString,
            @RequestHeader(name = ReCaptchaService.CAPTCHA_TOKEN_HEADER, required = false) String captchaValidationToken
    ) throws IOException, JSONException {
        log.info(String.format("Account registration initiated. Username: %s", accountInfoDTO.getUsername()));
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<AccountInfoDTO>> violations = validator.validate(accountInfoDTO);

        if (violations.size() > 0) {
            log.error(String.format("One or more errors occurred while creating the account. %s", violations.toArray()));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!Utils.isValidEmail(accountInfoDTO.getEmailAddress())) {
            log.error(String.format("Email is invalid. %s", accountInfoDTO.getEmailAddress()));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            JSONObject reCaptchaResponse = reCaptchaService.verifyToken(accountInfoDTO.getReCaptchaToken(), captchaValidationToken);
            log.info(String.format("reCaptcha response for %s: %s", accountInfoDTO.getUsername(), reCaptchaResponse.toString()));
        } catch (ReCaptchaLowScoreException v3Exception) {
            String jwtToken = jwtService.create();
            log.error(String.format("reCaptcha v3 error for: %s. description: %s", accountInfoDTO.getUsername(), v3Exception.getMessage()));
            return ResponseEntity.accepted().header(ReCaptchaService.CAPTCHA_TOKEN_HEADER, jwtToken).build();
        } catch (ReCaptchaUnsuccessfulResponseException v2Exception) {
            log.error(String.format("reCaptcha v2 error for: %s. description: %s", accountInfoDTO.getUsername(), v2Exception.getMessage()));
            return ResponseEntity.badRequest().build();
        } catch (JSONException jsonException) {
            log.error(String.format("JSONException while verifying reCaptcha token: %s.", jsonException.getMessage()));
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
            log.info("Decoding cip_authdata.");
            cipAuthDataDTO = cookieService.decodeCIPAuthDataCookie(cipAuthDataCookieString);
            account.setOpenIdProviderId(cipAuthDataDTO.getClientId());
            log.info("cip_authdata decoded. Provider clientId set.");
        } else {
            account.setOpenIdProviderId(defaultClientId);
            log.info("Blank cip_authdata. Setting default Provider ClientId.");
        }

        try {
            log.info(String.format("Creating new account for %s", account.getUsername()));
            CDCResponseData accountCreationResponse = accountsService.createAccount(account);
            String newAccountUid = accountCreationResponse.getUID();
            boolean isVerificationPending = EmailVerificationService.isVerificationPending(accountCreationResponse);
            account.setUid(newAccountUid);
            log.info(String.format("Account registration successful. Username: %s. UID: %s", account.getUsername(), newAccountUid));

            // TODO: Move into a service. Or simply create a model for this notification?
            String hashedPassword = HashingService.toMD5(account.getPassword());
            account.setPassword(hashedPassword);

            if (isRegistrationNotificationEnabled) {
                log.info(String.format("Sending account registered notification for UID: %s", newAccountUid));
                notificationService.sendAccountRegisteredNotification(account, cipdc);
                log.info(String.format("Account registered notification sent for UID: %s", newAccountUid));
            }

            log.info(String.format("Sending account info notification for UID: %s", newAccountUid));
            notificationService.sendNotifyAccountInfoNotification(account, cipdc);
            log.info(String.format("Account info notification sent for UID: %s", newAccountUid));

            // TODO: Check notifications possible error scenarios
            if (isAspireRegistrationValid(account)) {
                log.info(String.format("Sending Aspire registration notification for UID: %s", newAccountUid));
                notificationService.sendAspireRegistrationNotification(account);
                log.info(String.format("Aspire registration notification sent for UID: %s", newAccountUid));
            }

            if (account.getRegistrationType() != null && account.getRegistrationType().equals(RegistrationType.BASIC.getValue())) {
                log.info(String.format("Sending registration confirmation notification for UID: %s", newAccountUid));
                notificationService.sendConfirmationEmailNotification(account);
                log.info(String.format("Registration confirmation notification sent for UID: %s", newAccountUid));
            }

            if (accountCreationResponse.getErrorCode() == GigyaCodes.SUCCESS.getValue()) {
                log.info(String.format("Sending email verification notification for UID: %s", newAccountUid));
                emailVerificationService.sendVerificationByLinkEmail(newAccountUid);
                log.info(String.format("Email verification notification for UID: %s", newAccountUid));
            }

            if (isInvitedAccount(decryptedCiphertext)) {
                log.info("Updating invitation with country code.");
                JSONObject updateInvitationDTO = new JSONObject()
                        .put("inviteeUsername", account.getUsername())
                        .put("country", account.getCountry());
                Integer response = invitationService.updateInvitationCountry(updateInvitationDTO);
                if (response == HttpStatus.OK.value()) {
                    log.info("Invitation was updated successfully with country value.");
                } else {
                    log.info("An error occurred. Invitation was not updated with country value.");
                }
            }

            if (isVerificationPending && isInvitedAccount(decryptedCiphertext)) {
                log.info(String.format("Verifying email for invited user: %s", newAccountUid));
                CDCResponse verifyResponse = accountsService.verify(account, accountCreationResponse.getRegToken());
                accountCreationResponse.setErrorCode(verifyResponse.getErrorCode());
                accountCreationResponse.setStatusCode(verifyResponse.getStatusCode());
                accountCreationResponse.setStatusReason(verifyResponse.getStatusReason());
                log.info(String.format("Auto email verification successful for user: %s", newAccountUid));
            } else if (isVerificationPending) {
                log.info("Email verification pending. Returning response with error {}.", ResponseCode.ACCOUNT_PENDING_VERIFICATION.getValue());
                accountCreationResponse.setErrorCode(ResponseCode.ACCOUNT_PENDING_VERIFICATION.getValue());
            }

            return new ResponseEntity<>(accountCreationResponse, HttpStatus.OK);
        } catch (CustomGigyaErrorException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
        } catch (Exception e) {
            log.error(String.format("An error occurred while creating account for: %s , Error: %s", account.getUsername(), Utils.stackTraceToString(e)));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isInvitedAccount(Ciphertext ciphertext) {
        return ciphertext != null && InviteSource.contains(ciphertext.getSource());
    }

    private boolean isAspireRegistrationValid(AccountInfo accountInfoDTO) {
        return accountInfoDTO.getAcceptsAspireEnrollmentConsent() != null && accountInfoDTO.getAcceptsAspireEnrollmentConsent()
                && accountInfoDTO.getIsHealthcareProfessional() != null && !accountInfoDTO.getIsHealthcareProfessional()
                && accountInfoDTO.getAcceptsAspireTermsAndConditions() != null && accountInfoDTO.getAcceptsAspireTermsAndConditions();
    }

    @PostMapping("/merged")
    @Operation(description = "Updates account data based on the accountMerged event from CDC.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<String> onAccountsMerge(@RequestHeader("x-gigya-sig-jwt") String jwt, @RequestBody String body) {
        log.info(String.format("/accounts/merged called with body: %s", body));
        try {
            log.info("JWT validation started.");
            log.info("Getting JWT public key.");
            JWTPublicKey jwtPublicKey = gigyaService.getJWTPublicKey();
            log.info("Validating JWT signature.");
            boolean isJWTValid = JWTValidator.isValidSignature(jwt, jwtPublicKey);

            if (!isJWTValid) {
                log.info("Invalid JWT signature. Bad request.");
                return new ResponseEntity<>(HttpStatus.OK);
            }
            log.info("JWT signature valid.");

            JSONObject jsonBody = new JSONObject(body);
            JSONArray events = (JSONArray) jsonBody.get("events");

            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);

                if (isMergeEvent(event)) {
                    log.info("accountMerged webhook event fired.");
                    JSONObject data = (JSONObject) event.get("data");
                    String uid = data.get("newUid").toString();
                    accountsService.onAccountMerged(uid);
                } else if (isUpdateEvent(event)) {
                    JSONObject data = (JSONObject) event.get("data");
                    log.info("accountUpdate webhook event fired.");
                    String uid = data.get("uid").toString();
                    accountsService.onAccountUpdated(uid);
                }
            }
        } catch (Exception e) {
            log.error(String.format("onAccountsMerge error: %s. %s.", e.getMessage(), e.getCause()));
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
    @Operation(description = "Sends Username Recovery email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<String> sendRecoverUsernameEmail(@RequestBody @Valid UsernameRecoveryDTO usernameRecoveryDTO) {
        try {
            String email = usernameRecoveryDTO.getUserInfo().getEmail();
            log.info(String.format("Username recovery email requested by %s", email));
            AccountInfo account = gigyaService.getAccountInfoByEmail(email);

            if (account == null) {
                log.warn(String.format("No account found for %s while sending username recovery email.", email));
                return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
            }

            log.info(String.format("Sending username recovery email to %s", email));
            notificationService.sendRecoveryUsernameEmailNotification(usernameRecoveryDTO, account);
            log.info(String.format("Username recovery email sent to %s", email));

            return new ResponseEntity<>("OK", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/user/verification-email/{uid}")
    @Operation(description = "Triggers CDC email verification process.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @Parameter(name = "uid", description = "CDC user UID.", required = true, content = @Content(schema = @Schema(type = "string")))
    public ResponseEntity<CDCResponseData> sendVerificationEmail(@PathVariable String uid) {
        log.info(String.format("CDC verification email process triggered for user with UID: %s", uid));

        CDCResponseData responseData = emailVerificationService.sendVerificationByLinkEmailSync(uid);
        HttpStatus responseStatus = HttpStatus.valueOf(responseData.getStatusCode());

        return new ResponseEntity<>(responseData, responseStatus);
    }

    @PutMapping("/user/timezone")
    @Operation(description = "Sets the user's Timezone in CDC.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<String> setTimezone(@RequestBody @Valid UserTimezone userTimezone) throws JSONException, JsonProcessingException {
        HttpStatus updateUserTimezoneStatus = updateAccountService.updateTimezoneInCDC(userTimezone.getUid(), userTimezone.getTimezone());
        if (updateUserTimezoneStatus == HttpStatus.OK) {
            String description = String.format("User %s updated.", userTimezone.getUid());
            log.info(description);
            return new ResponseEntity<>(description, updateUserTimezoneStatus);
        } else {
            String description = String.format("An error occurred during the user's timezone update. UID: %s", userTimezone.getUid());
            log.error(description);
            return new ResponseEntity<>(description, updateUserTimezoneStatus);
        }
    }

    @GetMapping("/{loginID}")
    @Operation(description = "Checks whether a loginID is available in CDC.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK."),
            @ApiResponse(responseCode = "404", description = "Not found."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @Parameter(name = "loginID", description = "LoginID to be checked in CDC.", required = true)
    public ResponseEntity<AccountAvailabilityResponse> isAvailableLoginID(@PathVariable @NotBlank String loginID) {
        log.info(String.format("Check for loginID availability started. Login ID: %s", loginID));

        Boolean cdcAvailabilityResponse;
        try {
            cdcAvailabilityResponse = gigyaService.isAvailableLoginId(loginID);
        } catch (Exception e) {
            String exception = Utils.stackTraceToString(e);
            log.error(String.format("An error occurred while checking availability in CDC for: %s. Error: %s", loginID, exception));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
        }

        log.info(String.format("Is %s available in CDC: %b", loginID, cdcAvailabilityResponse));

        AccountAvailabilityResponse response = AccountAvailabilityResponse.builder()
                .isCDCAvailable(cdcAvailabilityResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/user")
    @Operation(description = "Update User Profile in CDC.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK."),
            @ApiResponse(responseCode = "400", description = "Bad Request."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<String> updateUserProfile(@RequestBody ProfileInfoDTO profileInfoDTO) throws CustomGigyaErrorException {
        try {
            if (profileInfoDTO == null || Utils.isNullOrEmpty(profileInfoDTO.getUid())) {
                log.error("ProfileInfoDTO is null or UID is null/empty");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            log.info(String.format("Starting user profile update process with UID: %s", profileInfoDTO.getUid()));
            String uid = profileInfoDTO.getUid();
            AccountInfo accountInfoDTO = gigyaService.getAccountInfo(uid);
            profileInfoDTO.setActualEmail(accountInfoDTO.getEmailAddress());
            profileInfoDTO.setActualUsername(accountInfoDTO.getUsername());
            HttpStatus updateUserProfileStatus = updateAccountService.updateProfile(profileInfoDTO);
            if (updateUserProfileStatus == HttpStatus.OK) {
                log.info(String.format("User %s updated.", uid));
                AccountInfo updatedAccountInfo = gigyaService.getAccountInfo(uid);
                log.info("Building AccountUpdatedNotification object.");
                AccountUpdatedNotification accountUpdatedNotification = AccountUpdatedNotification.build(updatedAccountInfo);
                log.info("Sending accountUpdated notification.");
                notificationService.sendPrivateAccountUpdatedNotification(accountUpdatedNotification);
                notificationService.sendPublicAccountUpdatedNotification(accountUpdatedNotification);
                log.info("accountUpdated notification sent.");
                return new ResponseEntity<String>("The information was successfully updated.", updateUserProfileStatus);
            } else {
                log.error(String.format("An error occurred during the user's profile update. UID: %s", uid));
                return new ResponseEntity<>(updateUserProfileStatus);
            }
        } catch (JSONException ex) {
            log.error(String.format("Internal server error : %s", ex.getMessage()));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/user/consent")
    @Operation(description = "Update user consent preferences")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK."),
            @ApiResponse(responseCode = "400", description = "Bad Request."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<?> updateConsent(@RequestBody @Valid ConsentDTO consentDTO) {
        try {
            accountsService.updateConsent(consentDTO);
            accountsService.notifyUpdatedConsent(consentDTO.getUid());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CustomGigyaErrorException e) {
            log.error("An unexpected error occurred when consuming CDC services", e);
            return new ResponseEntity<>(HttpStatus.FAILED_DEPENDENCY);
        } catch (Exception e) {
            log.error("An unexpected exception occurred.", e);
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
