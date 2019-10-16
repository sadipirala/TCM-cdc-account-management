package com.thermofisher.cdcam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.controller.AccountsController;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.UserDetails;
import com.thermofisher.cdcam.services.CDCAccountsService;
import com.thermofisher.cdcam.services.HashValidationService;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
import com.thermofisher.cdcam.utils.cdc.UsersHandler;
import org.json.JSONException;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class AccountsControllerTests {
    private String header = "test";
    private final String uid = "c1c691f4-556b-4ad1-ab75-841fc4e94dcd";
    private final List<String> uids = new ArrayList<>();
    private final List<String> emptyUIDs = new ArrayList<>();
    private final String username = "federatedUser@OIDC.com";
    private final String firstName = "first";
    private final String lastName = "last";
    private final int assoiciatedAccounts = 1;
    private final String hashedString = "QJERFC2183DASJ=";

    @InjectMocks
    AccountsController accountsController;

    @Mock
    CDCAccountsService cdcAccountsService;

    @Mock
    CDCAccounts cdcAccounts;

    @Mock
    LiteRegHandler mockLiteRegHandler;

    @Mock
    UsersHandler usersHandler;

    @Mock
    SecretsManager secretsManager;

    @Mock
    HashValidationService hashValidationService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        uids.add("001");
        uids.add("002");
        uids.add("003");
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn(hashedString);
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListEmpty_returnBadRequest()
            throws JsonProcessingException, JSONException {
        // given
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"eec-secret-key\":\"x\"}");

        List<String> emails = new ArrayList<>();
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListNull_returnBadRequest()
            throws JsonProcessingException, JSONException {
        // given
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"eec-secret-key\":\"x\"}");

        EmailList emailList = EmailList.builder().emails(null).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListHasValues_returnOK() throws IOException, JSONException {
        // given
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"eec-secret-key\":\"x\"}");

        List<EECUser> mockResult = new ArrayList<>();
        mockResult.add(Mockito.mock(EECUser.class));
        Mockito.when(mockLiteRegHandler.process(any())).thenReturn(mockResult);
        mockLiteRegHandler.requestLimit = 1000;
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void emailOnlyRegistration_WhenHandlerProcessThrowsException_returnInternalServerError() throws IOException, JSONException {
        // given
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"eec-secret-key\":\"x\"}");

        when(mockLiteRegHandler.process(any())).thenThrow(IOException.class);
        mockLiteRegHandler.requestLimit = 1000;
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void emailOnlyRegistration_WhenRequestLimitExceeded_returnBadRequest() throws IOException, JSONException {
        // given
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(mockLiteRegHandler.process(any())).thenThrow(IOException.class);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"eec-secret-key\":\"x\"}");

        mockLiteRegHandler.requestLimit = 1;
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenRequestHeaderInvalid_returnBadRequest() throws IOException, JSONException {
        // given
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(false);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"eec-secret-key\":\"x\"}");

        EmailList emailList = EmailList.builder().emails(null).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(header, emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void userUpdate_WhenHashSignatureIsDifferent_returnBadRequest() throws JsonProcessingException, JSONException {
        // given
        final String requestExceptionHeader = "Request-Exception";
        String badRequestHeaderMessage = "Invalid request header.";
        String invalidHash = String.format("%s=extraInvalidCharacters", hashedString);
        AccountInfo account = AccountInfo.builder().uid(uid).username(username).emailAddress(username).build();
        Mockito.when(cdcAccounts.getAccount(any())).thenReturn(account);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        // when
        ResponseEntity<String> res = accountsController.updateUser(invalidHash, "{\"test\":\"test\"}");

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        Assert.assertEquals(badRequestHeaderMessage, res.getHeaders().get(requestExceptionHeader).get(0));
    }

    @Test
    public void userUpdate_WhenCallingCDCAccountsService_WithErrorCodeShouldReturnResponseEntityWithError500() throws JsonProcessingException, JSONException {
        // given
        String message = "Internal server error.";
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("message", message);
        AccountInfo account = AccountInfo.builder().uid(uid).username(username).emailAddress(username).build();
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(cdcAccounts.getAccount(any())).thenReturn(account);
        Mockito.when(cdcAccountsService.update(any())).thenReturn(response);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        // when
        ResponseEntity<String> res = accountsController.updateUser(header, "{\"test\":\"test\"}");

        // then
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), res.getStatusCode().value());
        Assert.assertEquals(message, res.getBody());
    }

    @Test
    public void userUpdate_WhenCallingCDCAccountsService_WithSuccessCodeShouldReturnResponseEntityWithStatus200() throws JsonProcessingException, JSONException {
        // given
        String message = "OK";
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("code", HttpStatus.OK.value());
        response.put("message", message);
        AccountInfo account = AccountInfo.builder().uid(uid).username(username).emailAddress(username).build();
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(cdcAccounts.getAccount(any())).thenReturn(account);
        Mockito.when(cdcAccountsService.update(any())).thenReturn(response);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        // when
        ResponseEntity<String> res = accountsController.updateUser(header, "{\"test\":\"test\"}");

        // then
        Assert.assertEquals(ResponseEntity.ok().build().getStatusCode(), res.getStatusCode());
    }

    @Test
    public void getUser_GivenAValidUID_ShouldReturnUserDetails() throws IOException, JSONException {
        //setup
        UserDetails userDetails = UserDetails.builder().uid(uid).email(username).firstName(firstName).lastName(lastName).associatedAccounts(assoiciatedAccounts).build();
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(usersHandler.getUser(anyString())).thenReturn(userDetails);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        //execution
        ResponseEntity<UserDetails> resp = accountsController.getUser(header, uid);

        //validation
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.OK);

    }

    @Test
    public void getUser_GivenAInValidUID_ShouldReturnBadRequest() throws IOException, JSONException {
        //setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(usersHandler.getUser(anyString())).thenReturn(null);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        //execution
        ResponseEntity<UserDetails> resp = accountsController.getUser(header, uid);

        //validation
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.BAD_REQUEST);

    }

    @Test
    public void getUser_GivenAnIOError_ShouldThrowException() throws IOException, JSONException {
        //setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(usersHandler.getUser(anyString())).thenThrow(Exception.class);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        //execution
        ResponseEntity<UserDetails> resp = accountsController.getUser(header, uid);

        //validation
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Test
    public void getUser_GivenAnInvalidSHASignature_ShouldReturnBadRequest() throws JSONException {
        //setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(false);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        //execution
        ResponseEntity<UserDetails> resp = accountsController.getUser(header, uid);

        //validation
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.BAD_REQUEST);

    }

    @Test
    public void getUsers_GivenAValidListOfUID_ShouldReturnUserDetails() throws IOException, JSONException {
        //setup
        List<UserDetails> userDetailsList = new ArrayList<>();
        userDetailsList.add(UserDetails.builder().uid(uids.get(0)).email(username).firstName(firstName).lastName(lastName).associatedAccounts(assoiciatedAccounts).build());
        userDetailsList.add(UserDetails.builder().uid(uids.get(1)).email(username).firstName(firstName).lastName(lastName).associatedAccounts(assoiciatedAccounts).build());
        userDetailsList.add(UserDetails.builder().uid(uids.get(2)).email(username).firstName(firstName).lastName(lastName).associatedAccounts(assoiciatedAccounts).build());
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(usersHandler.getUsers(uids)).thenReturn(userDetailsList);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        //execution
        ResponseEntity<List<UserDetails>> resp = accountsController.getUsers(header, uids);

        //validation
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.OK);

    }

    @Test
    public void getUsers_GivenAnEmptyListOfUID_ShouldReturnBadRequest() throws IOException, JSONException {
        //setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(usersHandler.getUser(anyString())).thenReturn(null);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        //execution
        ResponseEntity<List<UserDetails>> resp = accountsController.getUsers(header, emptyUIDs);

        //validation
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.BAD_REQUEST);

    }

    @Test
    public void getUsers_GivenAnIOError_returnInternalServerError() throws IOException, JSONException {
        //setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(usersHandler.getUsers(uids)).thenThrow(Exception.class);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        //execution
        ResponseEntity<List<UserDetails>> resp = accountsController.getUsers(header, uids);

        //validation
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);

    }


    @Test
    public void getUsers_GivenAnInvalidSHASignature_ShouldReturnBadRequest() throws JSONException {
        //setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(false);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        //execution
        ResponseEntity<List<UserDetails>> resp = accountsController.getUsers(header, uids);

        //validation
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.BAD_REQUEST);

    }

    @Test
    public void handleHttpMessageNotReadableExceptions_givenHttpMessageNotReadableException_ReturnErrorMessage(){
        //setup
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("");

        //execution
        String resp = accountsController.handleHttpMessageNotReadableExceptions(ex);

        //validation
        Assert.assertEquals(resp,"Invalid input format. Message not readable.");
    }

    @Test
    public void handleHttpMessageNotReadableExceptions_givenParseException_ReturnErrorMessage(){
        //setup
        ParseException ex = new ParseException(1);

        //execution
        String resp = accountsController.handleHttpMessageNotReadableExceptions(ex);

        //validation
        Assert.assertEquals(resp,"Invalid input format. Message not readable.");
    }

}
