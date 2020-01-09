package com.thermofisher.cdcam;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.controller.AccountsController;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.UserDetails;
import com.thermofisher.cdcam.services.HashValidationService;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
import com.thermofisher.cdcam.utils.cdc.UsersHandler;
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
    public void emailOnlyRegistration_WhenEmailListEmpty_returnBadRequest() {
        // setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"eec-secret-key\":\"x\"}");

        List<String> emails = new ArrayList<>();
        EmailList emailList = EmailList.builder().emails(emails).build();

        // execution
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // validation
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListNull_returnBadRequest() {
        // setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"eec-secret-key\":\"x\"}");

        EmailList emailList = EmailList.builder().emails(null).build();

        // execution
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // validation
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListHasValues_returnOK() throws IOException {
        // setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"eec-secret-key\":\"x\"}");

        List<EECUser> mockResult = new ArrayList<>();
        mockResult.add(Mockito.mock(EECUser.class));
        Mockito.when(mockLiteRegHandler.process(any())).thenReturn(mockResult);
        mockLiteRegHandler.requestLimit = 1000;
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // execution
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // validation
        Assert.assertEquals(res.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void emailOnlyRegistration_WhenHandlerProcessThrowsException_returnInternalServerError() throws IOException {
        // setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"eec-secret-key\":\"x\"}");

        when(mockLiteRegHandler.process(any())).thenThrow(IOException.class);
        mockLiteRegHandler.requestLimit = 1000;
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // execution
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // validation
        Assert.assertEquals(res.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void emailOnlyRegistration_WhenRequestLimitExceeded_returnBadRequest() throws IOException {
        // setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(mockLiteRegHandler.process(any())).thenThrow(IOException.class);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"eec-secret-key\":\"x\"}");

        mockLiteRegHandler.requestLimit = 1;
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // execution
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // validation
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenRequestHeaderInvalid_returnBadRequest() {
        // setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(false);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"eec-secret-key\":\"x\"}");

        EmailList emailList = EmailList.builder().emails(null).build();

        // execution
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // validation
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getUsers_GivenAValidListOfUID_ShouldReturnUserDetails() throws IOException {
        // setup
        List<UserDetails> userDetailsList = new ArrayList<>();
        userDetailsList.add(UserDetails.builder().uid(uids.get(0)).email(username).firstName(firstName)
                .lastName(lastName).associatedAccounts(assoiciatedAccounts).build());
        userDetailsList.add(UserDetails.builder().uid(uids.get(1)).email(username).firstName(firstName)
                .lastName(lastName).associatedAccounts(assoiciatedAccounts).build());
        userDetailsList.add(UserDetails.builder().uid(uids.get(2)).email(username).firstName(firstName)
                .lastName(lastName).associatedAccounts(assoiciatedAccounts).build());
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(usersHandler.getUsers(uids)).thenReturn(userDetailsList);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        // execution
        ResponseEntity<List<UserDetails>> resp = accountsController.getUsers(uids);

        // validation
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.OK);

    }

    @Test
    public void getUsers_GivenAnEmptyListOfUID_ShouldReturnBadRequest() throws IOException {
        // setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(usersHandler.getUser(anyString())).thenReturn(null);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        // execution
        ResponseEntity<List<UserDetails>> resp = accountsController.getUsers(emptyUIDs);

        // validation
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.BAD_REQUEST);

    }

    @Test
    public void getUsers_GivenAnIOError_returnInternalServerError() throws IOException {
        // setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(usersHandler.getUsers(uids)).thenThrow(Exception.class);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        // execution
        ResponseEntity<List<UserDetails>> resp = accountsController.getUsers(uids);

        // validation
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Test
    public void getUsers_GivenAnInvalidSHASignature_ShouldReturnBadRequest() {
        // setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(false);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        // execution
        ResponseEntity<List<UserDetails>> resp = accountsController.getUsers(uids);

        // validation
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.BAD_REQUEST);

    }

    @Test
    public void handleHttpMessageNotReadableExceptions_givenHttpMessageNotReadableException_ReturnErrorMessage() {
        // setup
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("");

        // execution
        String resp = accountsController.handleHttpMessageNotReadableExceptions(ex);

        // validation
        Assert.assertEquals(resp, "Invalid input format. Message not readable.");
    }

    @Test
    public void handleHttpMessageNotReadableExceptions_givenParseException_ReturnErrorMessage() {
        // setup
        ParseException ex = new ParseException(1);

        // execution
        String resp = accountsController.handleHttpMessageNotReadableExceptions(ex);

        // validation
        Assert.assertEquals(resp, "Invalid input format. Message not readable.");
    }
}