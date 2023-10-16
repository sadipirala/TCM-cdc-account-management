package com.thermofisher.cdcam.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EECUserV2;
import com.thermofisher.cdcam.model.EECUserV3;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.dto.LiteAccountDTO;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.LiteRegistrationService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class EmailAccountsController {
    private static final String requestExceptionHeader = "Request-Exception";

    @Value("${eec.request.limit}")
    public int requestLimit;

    @Autowired
    LiteRegistrationService liteRegistrationService;
    
    @PostMapping("/v3/accounts/lite")
    @Operation(description = "Request enhanced lite-account registration from a list of users. V3")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request. No elements were sent or limit was exceeded.", headers = {
            @Header(name = requestExceptionHeader, description = "Response description", schema = @Schema(type = "string"))
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", headers = {
            @Header(name = requestExceptionHeader, description = "Response description",  schema = @Schema(type = "string"))
        })
    })
    public ResponseEntity<List<EECUserV3>> addLiteAccount(@Valid @RequestBody List<LiteAccountDTO> accountList) {
        log.info("Lite account registration initiated. V3");

        try {
            List<EECUserV3> response = liteRegistrationService.registerLiteAccounts(accountList);
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            String errorMessage = String.format("An error occurred during request validation. Error description: %s", e.getMessage());
            log.error(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(requestExceptionHeader, errorMessage).body(null);
        } catch (Exception e) {
            String errorMessage = String.format("An error occurred during email only registration process... %s", e.getMessage());
            log.error(errorMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header(requestExceptionHeader, errorMessage).body(null);
        }
    }

    @PostMapping("/v2/accounts/lite")
    @Operation(description = "Request email-only registration from a list of email addresses. V2")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request. Either no list elements were sent or limit was exceeded.", headers = {
            @Header(name = requestExceptionHeader, description = "Response description",  schema = @Schema(type = "string"))
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", headers = {
            @Header(name = requestExceptionHeader, description = "Response description",  schema = @Schema(type = "string"))
        })
    })
    @Parameter(name = "emailList", description = "List of emails to 'email-only' register", required = true,
            content = @Content(schema = @Schema(type = "EmailList")), in = ParameterIn.QUERY)
    public ResponseEntity<List<EECUserV2>> register(@Valid @RequestBody EmailList emailList) {
        log.info("Email only registration initiated.");

        if (Utils.isNullOrEmpty(emailList.getEmails())) {
            String errorMessage = "No users requested.";
            log.error(errorMessage);
            return ResponseEntity.badRequest().header(requestExceptionHeader, errorMessage).body(null);
        } else if (emailList.getEmails().size() > requestLimit) {
            String errorMessage = String.format("Requested users exceed request limit: %s.", requestLimit);
            log.error(errorMessage);
            return ResponseEntity.badRequest().header(requestExceptionHeader, errorMessage).body(null);
        }
        
        try {
            List<EECUserV2> response = liteRegistrationService.registerEmailAccounts(emailList);
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            String error = e.getMessage();
            log.error(error);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(requestExceptionHeader, error).body(null);
        } catch (Exception e) {
            String error = String.format("An error occurred during email only registration process... %s", e.getMessage());
            log.error(error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header(requestExceptionHeader, error).body(null);
        }
    }

    @Deprecated
    @PostMapping("/accounts/email-only/users")
    @Operation(description = "Request email-only registration from a list of email addresses.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request. Either no list elements were sent or limit was exceeded.", headers = {
            @Header(name = requestExceptionHeader, description = "Response description",  schema = @Schema(type = "string"))
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", headers = {
            @Header(name = requestExceptionHeader, description = "Response description",  schema = @Schema(type = "string"))
        })
    })
    @Parameter(name = "emailList", description = "List of emails to 'email-only' register", required = true, content = @Content(schema = @Schema(type = "EmailList", defaultValue = "0")), in = ParameterIn.QUERY)
    public ResponseEntity<List<EECUser>> emailOnlyRegistration(@Valid @RequestBody EmailList emailList) {
        log.info("Email only registration initiated.");

        if (Utils.isNullOrEmpty(emailList.getEmails())) {
            String errorMessage = "No users requested.";
            log.error(errorMessage);
            return ResponseEntity.badRequest().header(requestExceptionHeader, errorMessage).body(null);
        } else if (emailList.getEmails().size() > requestLimit) {
            String errorMessage = String.format("Requested users exceed request limit: %s.", requestLimit);
            log.error(errorMessage);
            return ResponseEntity.badRequest().header(requestExceptionHeader, errorMessage).body(null);
        }
        
        try {
            List<EECUser> response = liteRegistrationService.createLiteAccountsV1(emailList);
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            String error = e.getMessage();
            log.error(error);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(requestExceptionHeader, error).body(null);
        } catch (Exception e) {
            String error = String.format("An error occurred during email only registration process... %s", e.getMessage());
            log.error(error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header(requestExceptionHeader, error).body(null);
        }
    }
}
