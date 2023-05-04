package com.thermofisher.cdcam.controller;

import java.util.List;

import javax.validation.Valid;

import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EECUserV2;
import com.thermofisher.cdcam.model.EECUserV3;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.dto.LiteAccountDTO;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.LiteRegistrationService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

@RestController
public class EmailAccountsController {
    private Logger logger = LogManager.getLogger(this.getClass());
    private static final String requestExceptionHeader = "Request-Exception";

    @Value("${eec.request.limit}")
    public int requestLimit;

    @Autowired
    LiteRegistrationService liteRegistrationService;
    
    @PostMapping("/v3/accounts/lite")
    @ApiOperation(value = "Request enhanced lite-account registration from a list of users. V3")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Invalid request. No elements were sent or limit was exceeded.", responseHeaders = {
            @ResponseHeader(name = requestExceptionHeader, description = "Response description", response = String.class)
        }),
        @ApiResponse(code = 500, message = "Internal server error", responseHeaders = {
            @ResponseHeader(name = requestExceptionHeader, description = "Response description", response = String.class)
        })
    })
    public ResponseEntity<List<EECUserV3>> addLiteAccount(@Valid @RequestBody List<LiteAccountDTO> accountList) {
        logger.info("Lite account registration initiated. V3");

        try {
            List<EECUserV3> response = liteRegistrationService.registerLiteAccounts(accountList);
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            String errorMessage = String.format("An error occurred during request validation. Error message: %s", e.getMessage());
            logger.error(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(requestExceptionHeader, errorMessage).body(null);
        } catch (Exception e) {
            String errorMessage = String.format("An error occurred during email only registration process... %s", e.getMessage());
            logger.error(errorMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header(requestExceptionHeader, errorMessage).body(null);
        }
    }

    @PostMapping("/v2/accounts/lite")
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
    public ResponseEntity<List<EECUserV2>> register(@Valid @RequestBody EmailList emailList) {
        logger.info("Email only registration initiated.");

        if (Utils.isNullOrEmpty(emailList.getEmails())) {
            String errorMessage = "No users requested.";
            logger.error(errorMessage);
            return ResponseEntity.badRequest().header(requestExceptionHeader, errorMessage).body(null);
        } else if (emailList.getEmails().size() > requestLimit) {
            String errorMessage = String.format("Requested users exceed request limit: %s.", requestLimit);
            logger.error(errorMessage);
            return ResponseEntity.badRequest().header(requestExceptionHeader, errorMessage).body(null);
        }
        
        try {
            List<EECUserV2> response = liteRegistrationService.registerEmailAccounts(emailList);
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

    @Deprecated
    @PostMapping("/accounts/email-only/users")
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
        } else if (emailList.getEmails().size() > requestLimit) {
            String errorMessage = String.format("Requested users exceed request limit: %s.", requestLimit);
            logger.error(errorMessage);
            return ResponseEntity.badRequest().header(requestExceptionHeader, errorMessage).body(null);
        }
        
        try {
            List<EECUser> response = liteRegistrationService.createLiteAccountsV1(emailList);
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
}
