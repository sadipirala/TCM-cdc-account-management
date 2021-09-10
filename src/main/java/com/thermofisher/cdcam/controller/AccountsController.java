package com.thermofisher.cdcam.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.builders.EmailRequestBuilder;
import com.thermofisher.cdcam.enums.RegistrationType;
import com.thermofisher.cdcam.enums.aws.CdcamSecrets;
import com.thermofisher.cdcam.enums.cdc.WebhookEvent;
import com.thermofisher.cdcam.model.AccountAvailabilityResponse;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.EmailSentResponse;
import com.thermofisher.cdcam.model.UserDetails;
import com.thermofisher.cdcam.model.UserTimezone;
import com.thermofisher.cdcam.model.UsernameRecoveryEmailRequest;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.JWTPublicKey;
import com.thermofisher.cdcam.model.dto.AccountInfoDTO;
import com.thermofisher.cdcam.model.dto.ChangePasswordDTO;
import com.thermofisher.cdcam.model.dto.ProfileInfoDTO;
import com.thermofisher.cdcam.model.dto.UsernameRecoveryDTO;
import com.thermofisher.cdcam.model.notifications.AccountUpdatedNotification;
import com.thermofisher.cdcam.model.notifications.PasswordUpdateNotification;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaLowScoreException;
import com.thermofisher.cdcam.model.reCaptcha.ReCaptchaUnsuccessfulResponseException;
import com.thermofisher.cdcam.services.AccountRequestService;
import com.thermofisher.cdcam.services.DataProtectionService;
import com.thermofisher.cdcam.services.EmailService;
import com.thermofisher.cdcam.services.JWTValidator;
import com.thermofisher.cdcam.services.NotificationService;
import com.thermofisher.cdcam.services.ReCaptchaService;
import com.thermofisher.cdcam.services.SecretsService;
import com.thermofisher.cdcam.services.UpdateAccountService;
import com.thermofisher.cdcam.services.hashing.HashingService;
import com.thermofisher.cdcam.utils.PasswordUtils;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
import com.thermofisher.cdcam.utils.cdc.UsersHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

@RestController
@RequestMapping("/accounts")
public class AccountsController {
    private Logger logger = LogManager.getLogger(this.getClass());
    private static final String requestExceptionHeader = "Request-Exception";

    @Value("${legacy-email-verification.enabled}")
    Boolean isEmailVerificationEnabled;

    @Autowired
    AccountRequestService accountRequestService;

    @Autowired
    CDCResponseHandler cdcResponseHandler;

    @Autowired
    DataProtectionService dataProtectionService;

    @Autowired
    EmailService emailService;

    @Autowired
    LiteRegHandler liteRegHandler;

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
            cdcResponseHandler.changePassword(uid, body.getNewPassword(), body.getPassword());
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

    @PostMapping("/email-only/users")
    @ApiOperation(value = "Request email-only registration from a list of email addresses.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Invalid request. Either no list elements were sent or limit was exceeded.", responseHeaders = {
            @ResponseHeader(name = requestExceptionHeader, description = "Response description", response = String.class)
        }),
        @ApiResponse(code = 500, message = "Internal server error", responseHeaders = {
            @ResponseHeader(name = requestExceptionHeader, description = "Response description", response = String.class)
        })
    })
    @ApiImplicitParam(name = "emailList", value = "List of emails to 'email-only' register", required = true, dataType = "EmailList", paramType = "body")
    public ResponseEntity<List<EECUser>> emailOnlyRegistration(@Valid @RequestBody EmailList emailList) {
        logger.info("Email only registration initiated.");

        if (Utils.isNullOrEmpty(emailList.getEmails())) {
            String errorMessage = "No users requested.";
            logger.error(errorMessage);
            return ResponseEntity.badRequest().header(requestExceptionHeader, errorMessage).body(null);
        } else if (emailList.getEmails().size() > liteRegHandler.requestLimit) {
            String errorMessage = String.format("Requested users exceed request limit: %s.", liteRegHandler.requestLimit);
            logger.error(errorMessage);
            return ResponseEntity.badRequest().header(requestExceptionHeader, errorMessage).body(null);
        }
        
        try {
            List<EECUser> response = liteRegHandler.createLiteAccountsV1(emailList);
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            String error = e.getMessage();
            logger.error(error);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(requestExceptionHeader, error).body(null);
        } catch (Exception e) {
            String error = String.format("An error occurred during email only registration process... %s", e.getMessage());
            logger.error(error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header(requestExceptionHeader, error).body(null);
        }
    }

    @PostMapping("/v2/email-only/users")
    @ApiOperation(value = "Request email-only registration from a list of email addresses. V2")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Invalid request. Either no list elements were sent or limit was exceeded.", responseHeaders = {
            @ResponseHeader(name = requestExceptionHeader, description = "Response description", response = String.class)
        }),
        @ApiResponse(code = 500, message = "Internal server error", responseHeaders = {
            @ResponseHeader(name = requestExceptionHeader, description = "Response description", response = String.class)
        })
    })
    @ApiImplicitParam(name = "emailList", value = "List of emails to 'email-only' register", required = true, dataType = "EmailList", paramType = "body")
    public ResponseEntity<List<EECUser>> emailOnlyRegistrationV2(@Valid @RequestBody EmailList emailList) {
        logger.info("Email only registration initiated.");

        if (Utils.isNullOrEmpty(emailList.getEmails())) {
            String errorMessage = "No users requested.";
            logger.error(errorMessage);
            return ResponseEntity.badRequest().header(requestExceptionHeader, errorMessage).body(null);
        } else if (emailList.getEmails().size() > liteRegHandler.requestLimit) {
            String errorMessage = String.format("Requested users exceed request limit: %s.", liteRegHandler.requestLimit);
            logger.error(errorMessage);
            return ResponseEntity.badRequest().header(requestExceptionHeader, errorMessage).body(null);
        }
        
        try {
            List<EECUser> response = liteRegHandler.createLiteAccountsV2(emailList);
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            String error = e.getMessage();
            logger.error(error);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(requestExceptionHeader, error).body(null);
        } catch (Exception e) {
            String error = String.format("An error occurred during email only registration process... %s", e.getMessage());
            logger.error(error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header(requestExceptionHeader, error).body(null);
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
    public ResponseEntity<ProfileInfoDTO> getUserProfileByUID(@PathVariable String uid){
        try{
            logger.info("User profile data by UID requested.");
            ProfileInfoDTO profileInfoDTO = usersHandler.getUserProfileByUID(uid);

            logger.info(String.format("Retrieved user with UID: %s", uid));
            return (profileInfoDTO != null) ? new ResponseEntity<ProfileInfoDTO>(profileInfoDTO, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
            JWTPublicKey jwtPublicKey = cdcResponseHandler.getJWTPublicKey();
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
                logger.info("onAccountRegistered initiated.");
                accountRequestService.onAccountRegistered(uid);
            }
        } catch (Exception e) {
            logger.error(String.format("onAccountRegistered error: %s. %s.", e.getMessage(), e.getCause()));
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/")
    @ApiOperation(value = "Inserts a new Account")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<CDCResponseData> newAccount(@RequestBody @Valid AccountInfoDTO accountInfo) throws IOException {
        logger.info(String.format("Account registration initiated. Username: %s", accountInfo.getUsername()));

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<AccountInfoDTO>> violations = validator.validate(accountInfo);

        if (violations.size() > 0) {
            logger.error(String.format("One or more errors occurred while creating the account. %s", violations.toArray()));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        try {
            String reCaptchaSecretKey = accountInfo.getIsReCaptchaV2() ? CdcamSecrets.RECAPTCHAV2.getKey() : CdcamSecrets.RECAPTCHAV3.getKey();
            String reCaptchaSecret = secretsService.get(reCaptchaSecretKey);
            JSONObject reCaptchaResponse = reCaptchaService.verifyToken(accountInfo.getReCaptchaToken(), reCaptchaSecret);
            logger.info(String.format("reCaptcha response for %s: %s", accountInfo.getUsername(), reCaptchaResponse.toString()));
        } catch (ReCaptchaLowScoreException v3Exception) {
            logger.error(String.format("reCaptcha v3 error for: %s. message: %s", accountInfo.getUsername(), v3Exception.getMessage()));
            return ResponseEntity.accepted().build();
        } catch (ReCaptchaUnsuccessfulResponseException v2Exception) {
            logger.error(String.format("reCaptcha v2 error for: %s. message: %s", accountInfo.getUsername(), v2Exception.getMessage()));
            return ResponseEntity.badRequest().build();
        } catch (JSONException jsonException) {
            logger.error(String.format("JSONException while verifying reCaptcha token: %s.", jsonException.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        String ciphertext = accountInfo.getCiphertext();

        if (ciphertext != null && !ciphertext.isEmpty()) {
            try{
                JSONObject decryptedUserData = dataProtectionService.decrypt(ciphertext);
                logger.info(String.format("Decryption response for %s: %s", accountInfo.getUsername(), decryptedUserData.toString()));
                accountInfo.setFirstName(decryptedUserData.getJSONObject("body").getString("firstName"));
                accountInfo.setLastName(decryptedUserData.getJSONObject("body").getString("lastName"));
                accountInfo.setEmailAddress(decryptedUserData.getJSONObject("body").getString("email"));
            }
            catch (JSONException exception) {
                logger.error(String.format("Data decryption error for : %s. message: %s", accountInfo.getUsername(), exception.getMessage()));
            }
        }

        AccountInfo account = AccountBuilder.parseFromAccountInfoDTO(accountInfo);
        CDCResponseData cdcResponseData = accountRequestService.processRegistrationRequest(account);

        if (cdcResponseData != null) {
            int statusCode = cdcResponseData.getStatusCode();

            if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                String uid = account.getUid();
                logger.info(String.format("Account registration successful. Username: %s. UID: %s", account.getUsername(), uid));

                if (isAspireRegistrationValid(account)) {
                    notificationService.sendAspireRegistrationNotification(account);
                    logger.info(String.format("Aspire Registration Notification sent successfully. UID: %s", uid));
                }

                if (account.getRegistrationType() != null && account.getRegistrationType().equals(RegistrationType.BASIC.getValue())) {
                    logger.info(String.format("Attempting to send confirmation email. UID: %s", uid));
                    accountRequestService.sendConfirmationEmail(account);
                }

                if (isEmailVerificationEnabled) {
                    logger.info(String.format("Attempting to send verification email to user. UID: %s", uid));
                    accountRequestService.sendVerificationEmail(uid);
                }
            } else {
                logger.warn(String.format("Account registration request failed. Username: %s. Status: %d. Details: %s",
                        account.getUsername(), statusCode, cdcResponseData.getErrorDetails()));
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(cdcResponseData, HttpStatus.OK);
        } else {
            logger.error(String.format("An error occurred while creating account for: %s", account.getUsername()));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isAspireRegistrationValid (AccountInfo accountInfo) {
        return accountInfo.getAcceptsAspireEnrollmentConsent() != null && accountInfo.getAcceptsAspireEnrollmentConsent()
            && accountInfo.getIsHealthcareProfessional() != null && !accountInfo.getIsHealthcareProfessional()
            && accountInfo.getAcceptsAspireTermsAndConditions() != null && accountInfo.getAcceptsAspireTermsAndConditions();
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
            JWTPublicKey jwtPublicKey = cdcResponseHandler.getJWTPublicKey();
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
                    accountRequestService.onAccountMerged(uid);
                } else if (isUpdateEvent(event)) {
                    JSONObject data = (JSONObject) event.get("data");
                    logger.info("accountUpdate webhook event fired.");
                    String uid = data.get("uid").toString();
                    accountRequestService.onAccountUpdated(uid);
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
            AccountInfo account = cdcResponseHandler.getAccountInfoByEmail(usernameRecoveryDTO.getUserInfo().getEmail());

            if (account == null) {
                logger.warn(String.format("No account found for email: %s", usernameRecoveryDTO.getUserInfo().getEmail()));
                return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
            }

            UsernameRecoveryEmailRequest usernameRecoveryEmailRequest = EmailRequestBuilder.buildUsernameRecoveryEmailRequest(usernameRecoveryDTO, account);
            EmailSentResponse response = emailService.sendUsernameRecoveryEmail(usernameRecoveryEmailRequest);
            
            if (!response.isSuccess()) {
                logger.error(String.format("Failed to send username recovery email to: %s", usernameRecoveryDTO.getUserInfo().getEmail()));
                return new ResponseEntity<>("There was an error sending username recovery email.", HttpStatus.BAD_REQUEST);
            }
            logger.info(String.format("Username recovery email sent to email address: %s", usernameRecoveryDTO.getUserInfo().getEmail()));
            return new ResponseEntity<>("OK", HttpStatus.OK);
        } catch (Exception e) {
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

        CDCResponseData responseData = accountRequestService.sendVerificationEmailSync(uid);
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
            cdcAvailabilityResponse = cdcResponseHandler.isAvailableLoginId(loginID);
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
            AccountInfo accountInfo = cdcResponseHandler.getAccountInfo(uid);
            profileInfoDTO.setActualEmail(accountInfo.getEmailAddress());
            profileInfoDTO.setActualUsername(accountInfo.getUsername());
            HttpStatus updateUserProfileStatus = updateAccountService.updateProfile(profileInfoDTO);
            if (updateUserProfileStatus == HttpStatus.OK) {
                logger.info(String.format("User %s updated.", uid));

                AccountInfo updatedAccountInfo = cdcResponseHandler.getAccountInfo(uid);
                logger.info("Building AccountUpdatedNotification object.");
                AccountUpdatedNotification accountUpdatedNotification = AccountUpdatedNotification.build(updatedAccountInfo);
                logger.info("Sending accountUpdated notification.");
                notificationService.sendAccountUpdatedNotification(accountUpdatedNotification);
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
    public String handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        return "Invalid input format.";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = NullPointerException.class)
    public String handleNullPointerException(
            NullPointerException ex) {
        return "Invalid input. Null body present.";
    }
}
