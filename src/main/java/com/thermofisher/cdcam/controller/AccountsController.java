package com.thermofisher.cdcam.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.UserDetails;
import com.thermofisher.cdcam.services.*;
import com.thermofisher.cdcam.utils.AccountInfoHandler;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
import com.thermofisher.cdcam.utils.cdc.UsersHandler;
import io.swagger.annotations.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountsController {
    static final Logger logger = LogManager.getLogger("CdcamApp");
    static final String requestExceptionHeader = "Request-Exception";

    @Value("${federation.aws.secret}")
    private String federationSecret;

    @Value("${cdcam.reg.notification.url}")
    private String regNotificationUrl;

    @Autowired
    CDCAccounts cdcAccounts;

    @Autowired
    SecretsManager secretsManager;

    @Autowired
    SNSHandler snsHandler;

    @Autowired
    LiteRegHandler handler;

    @Autowired
    AccountInfoHandler accountHandler;

    @Autowired
    UsersHandler usersHandler;

    @Autowired
    CDCAccountsService cdcAccountsService;

    @Autowired
    HashValidationService hashValidationService;

    @Autowired
    CDCAccountsService accountsService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    UpdateAccountService updateAccountService;

    @Autowired
    AccountRequestService accountRequestService;

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
        if (emailList.getEmails() == null || emailList.getEmails().size() == 0) {
            String errorMessage = "No users requested.";
            logger.error(errorMessage);
            return ResponseEntity.badRequest().header(requestExceptionHeader, errorMessage).body(null);
        } else if (emailList.getEmails().size() > handler.requestLimit) {
            String errorMessage = String.format("Requested users exceed request limit [%s].", handler.requestLimit);
            logger.error(errorMessage);
            return ResponseEntity.badRequest().header(requestExceptionHeader, errorMessage).body(null);
        } else {
            try {
                List<EECUser> response = handler.process(emailList);
                return ResponseEntity.ok().body(response);
            } catch (IOException e) {
                String errorMessage = String.format("An error occurred during EEC email only registration process... [%s]", e.toString());
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
    public ResponseEntity<List<UserDetails>> getUsers(@PathVariable List<String> uids)  {
        try {
            List<UserDetails> userDetails = usersHandler.getUsers(uids);
            return (userDetails.size() > 0) ? new ResponseEntity<>(userDetails, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
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
    public ResponseEntity<String> notifyRegistration(@RequestHeader("x-gigya-sig-hmac-sha1") String headerValue, @RequestBody String rawBody){
        accountRequestService.processRequest(headerValue,rawBody);
        return new ResponseEntity<>(HttpStatus.OK);
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