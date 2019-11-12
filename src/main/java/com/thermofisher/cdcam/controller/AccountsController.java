package com.thermofisher.cdcam.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.enums.cdc.Events;
import com.thermofisher.cdcam.enums.cdc.FederationProviders;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.UserDetails;
import com.thermofisher.cdcam.services.CDCAccountsService;
import com.thermofisher.cdcam.services.HashValidationService;
import com.thermofisher.cdcam.services.NotificationService;
import com.thermofisher.cdcam.utils.AccountInfoHandler;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
import com.thermofisher.cdcam.utils.cdc.UsersHandler;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
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
    static final String eecKey = "eec-secret-key";
    static final String fedKey = "cdc-secret-key";

    @Value("${eec.aws.secret}")
    private String eecSecret;

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
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header(requestExceptionHeader, "An error occurred during EEC email only registration process...").body(null);
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

    private boolean isValidHeader(String secret, String property, String data, String receivedHashString) throws JSONException {
        JSONObject secretProperties = new JSONObject(secret);
        String secretKey = Utils.getStringFromJSON(secretProperties, property);
        String generatedHashString = hashValidationService.getHashedString(secretKey, data);

        return hashValidationService.isValidHash(generatedHashString, receivedHashString);
    }

    @PostMapping("/user")
    @ApiOperation(value = "Create account in CDC.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    public ResponseEntity<String> notifyRegistration(@RequestHeader("x-gigya-sig-hmac-sha1") String headerValue, @RequestBody String rawBody){
        final int FED_PASSWORD_LENGTH = 10;

        try {
            org.json.simple.JSONObject secretProperties = (org.json.simple.JSONObject) new JSONParser().parse(secretsManager.getSecret(federationSecret));
            String key = secretsManager.getProperty(secretProperties, "cdc-secret-key");
            String hash = hashValidationService.getHashedString(key, rawBody);

            if (!hashValidationService.isValidHash(hash, headerValue)) {
                logger.error("INVALID SIGNATURE");
                return new ResponseEntity<>("INVALID SIGNATURE", HttpStatus.BAD_REQUEST);
            }

            JSONParser parser = new JSONParser();
            org.json.simple.JSONObject mainObject = (org.json.simple.JSONObject) parser.parse(rawBody);
            JSONArray events = (JSONArray) mainObject.get("events");

            for (Object singleEvent : events) {
                org.json.simple.JSONObject event = (org.json.simple.JSONObject) singleEvent;
                org.json.simple.JSONObject data = (org.json.simple.JSONObject) event.get("data");

                if (!event.get("type").equals(Events.REGISTRATION.getValue())) {
                    logger.error("The event type was not recognized");
                    return new ResponseEntity<>("the event type was not recognized", HttpStatus.OK);
                }

                String uid = data.get("uid").toString();
                AccountInfo account = accountsService.getAccountInfo(uid);
                if (account == null) {
                    logger.error("Account not found. UID: " + uid);
                    return new ResponseEntity<>("Account not found.", HttpStatus.BAD_REQUEST);
                }

                String accountToNotify = accountHandler.prepareForProfileInfoNotification(account);
                try {
                    CloseableHttpResponse response = notificationService.postRequest(accountToNotify, regNotificationUrl);
                    logger.info("Response:  " + response.getStatusLine().getStatusCode() + ". Response message: " + EntityUtils.toString(response.getEntity()));
                    response.close();
                }
                catch (Exception e) {
                    logger.error("EXCEPTION: The call to " + regNotificationUrl + " has failed with errors " + e.getMessage());
                }

                if (!hasFederationProvider(account)) {
                    logger.error("The user was not created through federation.");
                    return new ResponseEntity<>("The user was not created through federation.", HttpStatus.OK);
                }
                account.setPassword(Utils.getAlphaNumericString(FED_PASSWORD_LENGTH));

                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writeValueAsString(account);
                if (!snsHandler.sendSNSNotification(jsonString)) {
                    logger.error("The user was not created through federation.");
                    return new ResponseEntity<>("Something went wrong... An SNS Notification failed to be sent.", HttpStatus.SERVICE_UNAVAILABLE);
                }

                logger.info("User sent to SNS.");
                return new ResponseEntity<>(jsonString, HttpStatus.OK);
            }

            logger.error("NO EVENT FOUND");
            return new ResponseEntity<>("NO EVENT FOUND", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            logger.error(stackTrace);
            return new ResponseEntity<>("ERROR: " + stackTrace, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean hasFederationProvider(AccountInfo account) {
        return account.getLoginProvider().toLowerCase().contains(FederationProviders.OIDC.getValue()) || account.getLoginProvider().toLowerCase().contains(FederationProviders.SAML.getValue());
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
