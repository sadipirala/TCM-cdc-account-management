package com.thermofisher.cdcam.controller;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thermofisher.cdcam.builders.EmailRequestBuilder;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.enums.RegistrationType;
import com.thermofisher.cdcam.model.*;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.UserDetails;
import com.thermofisher.cdcam.model.UserTimezone;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.dto.AccountInfoDTO;

import com.thermofisher.cdcam.model.dto.UsernameRecoveryDTO;
import com.thermofisher.cdcam.services.AccountRequestService;
import com.thermofisher.cdcam.services.EmailService;

import com.thermofisher.cdcam.services.ReCaptchaService;
import com.thermofisher.cdcam.services.UpdateAccountService;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
import com.thermofisher.cdcam.utils.cdc.UsersHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import javax.validation.*;
import java.util.Set;

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

    @Value("${registration.recaptcha.secret.key}")
    private String registrationReCaptchaSecret;

    @Autowired
    LiteRegHandler handler;

    @Autowired
    UsersHandler usersHandler;

    @Autowired
    AccountRequestService accountRequestService;

    @Autowired
    UpdateAccountService updateAccountService;

    @Autowired
    CDCResponseHandler cdcResponseHandler;

    @Autowired
    EmailService emailService;

    @Autowired
    ReCaptchaService reCaptchaService;


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
        if (emailList.getEmails() == null || emailList.getEmails().size() == 0) {
            logger.warn("No users requested.");
            return ResponseEntity.badRequest().header(requestExceptionHeader, "No users requested.").body(null);
        } else if (emailList.getEmails().size() > handler.requestLimit) {
            String errorMessage = String.format("Requested users exceed request limit: %s.", handler.requestLimit);
            logger.error(errorMessage);
            return ResponseEntity.badRequest().header(requestExceptionHeader, errorMessage).body(null);
        } else {
            try {
                List<EECUser> response = handler.process(emailList);
                return ResponseEntity.ok().body(response);
            } catch (IOException e) {
                String errorMessage = String.format("An error occurred during email only registration process... %s", e.toString());
                logger.error(errorMessage);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header(requestExceptionHeader, errorMessage).body(null);
            }
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
        } catch (Exception e) {
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
    public ResponseEntity<String> notifyRegistration(@RequestHeader("x-gigya-sig-hmac-sha1") String headerValue, @RequestBody String rawBody) {
        logger.info("Notify registration initiated.");
        accountRequestService.processRequest(headerValue, rawBody);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private boolean isReCaptchaResponseInvalid(JSONObject reCaptchaResponse) throws JSONException {
        final String SUCCESS = "success";
        return reCaptchaResponse.has(SUCCESS) && !reCaptchaResponse.getBoolean(SUCCESS);
    }

    @PostMapping("/")
    @ApiOperation(value = "Inserts a new Account")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<CDCResponseData> newAccount(@RequestBody @Valid AccountInfoDTO accountInfo) throws IOException,
            JSONException {
        logger.info(String.format("Account registration initiated. Username: %s", accountInfo.getUsername()));

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<AccountInfoDTO>> violations = validator.validate(accountInfo);

        if (violations.size() > 0) {
            logger.error(String.format("One or more errors occurred while creating the account. %s", violations.toArray()));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        JSONObject reCaptchaResponse = reCaptchaService.verifyToken(accountInfo.getReCaptchaToken(), registrationReCaptchaSecret);
        if (isReCaptchaResponseInvalid(reCaptchaResponse)) {
            String errorCodes = reCaptchaResponse.get("error-codes").toString();
            logger.warn(String.format("reCaptcha token verification error codes: %s. Username: %s", errorCodes, accountInfo.getUsername()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(requestExceptionHeader, errorCodes).body(null);
        }

        AccountInfo account = AccountBuilder.parseFromAccountInfoDTO(accountInfo);
        CDCResponseData cdcResponseData = accountRequestService.processRegistrationRequest(account);

        if (cdcResponseData != null) {
            int statusCode = cdcResponseData.getStatusCode();

            if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                String uid = account.getUid();
                logger.info(String.format("Account registration successful. Username: %s. UID: %s", account.getUsername(), uid));

                if (account.getRegistrationType() != null && account.getRegistrationType().equals(RegistrationType.BASIC.getValue())) {
                    logger.info(String.format("Attempting to send confirmation email. UID: %s", uid));
                    accountRequestService.sendConfirmationEmail(account);
                }

                logger.info(String.format("Attempting to send verification email to user. UID: %s", uid));
                accountRequestService.sendVerificationEmail(uid);
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
                logger.warn("No account found for email: %s", usernameRecoveryDTO.getUserInfo().getEmail());
                return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
            }

            UsernameRecoveryEmailRequest usernameRecoveryEmailRequest = EmailRequestBuilder.buildUsernameRecoveryEmailRequest(usernameRecoveryDTO, account);
            EmailSentResponse response = emailService.sendUsernameRecoveryEmail(usernameRecoveryEmailRequest);
            
            if (!response.isSuccess()) {
                logger.error("Failed to send username recovery email to: %s", usernameRecoveryDTO.getUserInfo().getEmail());
                return new ResponseEntity<>("There was an error sending username recovery email.", HttpStatus.BAD_REQUEST);
            }
            logger.info("Username recovery email sent to email address: %s", usernameRecoveryDTO.getUserInfo().getEmail());
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
