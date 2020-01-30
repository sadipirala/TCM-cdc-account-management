package com.thermofisher.cdcam.controller;

import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.UserDetails;
import com.thermofisher.cdcam.model.UserTimezone;
import com.thermofisher.cdcam.services.AccountRequestService;
import com.thermofisher.cdcam.services.UpdateAccountService;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
import com.thermofisher.cdcam.utils.cdc.UsersHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
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
    static final Logger logger = LogManager.getLogger("CdcamApp");
    static final String requestExceptionHeader = "Request-Exception";

    @Autowired
    LiteRegHandler handler;

    @Autowired
    UsersHandler usersHandler;

    @Autowired
    AccountRequestService accountRequestService;

    @Autowired
    UpdateAccountService uptadeAccountService;

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

    @PutMapping("/user/timezone")
    @ApiOperation(value = "Sets the user's Timezone in CDC.")
    @ApiResponses({ 
            @ApiResponse(code = 200, message = "OK"), 
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.") 
    })
    public ResponseEntity<String> setTimezone(@RequestBody @Valid UserTimezone userTimezone)throws JSONException, JsonProcessingException {
        HttpStatus updateUserTimezoneStatus = uptadeAccountService.updateTimezoneInCDC(userTimezone.getUid(), userTimezone.getTimezone());
        if (updateUserTimezoneStatus == HttpStatus.OK) {
            String successMessage = String.format("User %s updated.", userTimezone.getUid());
            return new ResponseEntity<String>(successMessage, updateUserTimezoneStatus);
        } else {
            String errorMessage = "An error occurred during the user's timezone update.";
            logger.error(errorMessage);
            return new ResponseEntity<String>(errorMessage, updateUserTimezoneStatus);
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
