package com.thermofisher.cdcam.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.UserDetails;
import com.thermofisher.cdcam.services.CDCAccountsService;
import com.thermofisher.cdcam.services.HashValidationService;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
import com.thermofisher.cdcam.utils.cdc.UsersHandler;
import io.swagger.annotations.*;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountsController {
    static final Logger logger = LogManager.getLogger("CdcamApp");
    static final String requestExceptionHeader = "Request-Exception";

    @Value("${eec.aws.secret}")
    private String eecSecret;

    @Value("${federation.aws.secret}")
    private String federationSecret;

    @Autowired
    CDCAccounts cdcAccounts;

    @Autowired
    SecretsManager secretsManager;

    @Autowired
    LiteRegHandler handler;

    @Autowired
    UsersHandler usersHandler;

    @Autowired
    CDCAccountsService cdcAccountsService;

    @Autowired
    HashValidationService hashValidationService;

    @PostMapping("/eec/emails")
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
    public ResponseEntity<List<EECUser>> emailOnlyRegistration(@RequestHeader("x-eec-sig-hmac-sha1") String headerHashSignature, @Valid @RequestBody EmailList emailList)
            throws JsonProcessingException, JSONException {
        if(!isValidHeader(secretsManager.getSecret(eecSecret), "eec-secret-key", Utils.convertJavaToJsonString(emailList), headerHashSignature))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(requestExceptionHeader, "Invalid request header.").body(null);

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
                logger.fatal(errorMessage);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header(requestExceptionHeader, "An error occurred during EEC email only registration process...").body(null);
            }
        }
    }

    @PutMapping("/federation/user")
    @ApiOperation(value = "Updates user's data in CDC.",
            notes = "Keep in mind that the user's username should match the one in CDC.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<String> updateUser(@RequestHeader("x-fed-sig") String headerHashSignature, @NotEmpty @NotNull @RequestBody String body) throws JSONException {
        JSONObject jsonBody = Utils.convertStringToJson(body);
        if (jsonBody == null) return ResponseEntity.badRequest().header(requestExceptionHeader, "Body cannot be empty or null").body(null);

        if(!isValidHeader(secretsManager.getSecret(federationSecret), "cdc-secret-key", jsonBody.toString(), headerHashSignature))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(requestExceptionHeader, "Invalid request header.").body(null);

        ObjectNode response = cdcAccountsService.update(jsonBody);

        if (response == null) ResponseEntity.badRequest().header(requestExceptionHeader, "Invalid body structure").body(null);

        if (response.get("code").asInt() == HttpStatus.INTERNAL_SERVER_ERROR.value())
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(response.get("message").asText());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{uid}")
    @ApiOperation(value = "Gets a user")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<UserDetails> getUser(@RequestHeader("x-user-sig") String headerHashSignature, @PathVariable String uid) throws JSONException {
        if(!isValidHeader(secretsManager.getSecret(federationSecret), "cdc-secret-key", uid, headerHashSignature))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(requestExceptionHeader, "Invalid request header.").body(null);

        try {
            UserDetails userDetails = usersHandler.getUser(uid);
            return (userDetails != null) ? new ResponseEntity<>(userDetails, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/users/{uids}")
    @ApiOperation(value = "Gets a list of users")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<List<UserDetails>> getUsers(@RequestHeader("x-user-sig") String headerHashSignature, @PathVariable List<String> uids) throws JSONException {
        if(!isValidHeader(secretsManager.getSecret(federationSecret), "cdc-secret-key", String.join(",",uids), headerHashSignature))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(requestExceptionHeader, "Invalid request header.").body(null);

        try {
            List<UserDetails> userDetails = usersHandler.getUsers(uids);
            return (userDetails.size() > 0) ? new ResponseEntity<>(userDetails, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isValidHeader(String secret, String property, String data, String header) throws JSONException {
        JSONObject secretProperties = new JSONObject(secret);
        String secretKey = Utils.getStringFromJSON(secretProperties, property);
        String hash = hashValidationService.getHashedString(secretKey, data);

        logger.fatal("Data: " + data);
        logger.fatal("Key: " + secretKey);
        logger.fatal("Hash: " + hash);

        return hashValidationService.isValidHash(hash, header);
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
