package com.thermofisher.cdcam;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.controller.AccountsController;
import com.thermofisher.cdcam.controller.FederationController;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.UserDetails;
import com.thermofisher.cdcam.services.CDCAccountsService;
import com.thermofisher.cdcam.services.HashValidationService;
import com.thermofisher.cdcam.services.NotificationService;
import com.thermofisher.cdcam.utils.AccountInfoHandler;
import com.thermofisher.cdcam.utils.AccountInfoUtils;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
import com.thermofisher.cdcam.utils.cdc.UsersHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.BasicHttpEntity;
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
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class AccountsControllerTests {
    private String header = "test";
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
    AccountInfoHandler accountInfoHandler;

    @Mock
    LiteRegHandler mockLiteRegHandler;

    @Mock
    UsersHandler usersHandler;

    @Mock
    SNSHandler snsHandler;

    @Mock
    SecretsManager secretsManager;

    @Mock
    HashValidationService hashValidationService;

    @Mock
    CDCAccountsService accountsService;

    @Mock
    CDCAccountsService cdcAccountsService;

    @Mock
    NotificationService notificationService;

    private AccountInfo federationAccount = AccountInfo.builder()
            .username("federatedUser@OIDC.com")
            .emailAddress("federatedUser@OIDC.com")
            .firstName("first")
            .lastName("last")
            .country("country")
            .localeName("en_US")
            .loginProvider("oidc")
            .password("Password1")
            .regAttempts(0)
            .city("testCity")
            .department("dep")
            .company("myCompany")
            .build();

    private AccountInfo nonFederationAccount = AccountInfo.builder()
            .username("User@test.com")
            .emailAddress("User@test.com")
            .firstName("first")
            .lastName("last")
            .country("country")
            .localeName("en_US")
            .loginProvider("nonfederation")
            .password("Password1")
            .regAttempts(0)
            .city("testCity")
            .department("dep")
            .company("myCompany")
            .build();

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
        // given
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"eec-secret-key\":\"x\"}");

        EmailList emailList = EmailList.builder().emails(null).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenEmailListHasValues_returnOK() throws IOException {
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
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void emailOnlyRegistration_WhenHandlerProcessThrowsException_returnInternalServerError() throws IOException {
        // given
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"eec-secret-key\":\"x\"}");

        when(mockLiteRegHandler.process(any())).thenThrow(IOException.class);
        mockLiteRegHandler.requestLimit = 1000;
        List<String> emails = new ArrayList<>();
        emails.add("email1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void emailOnlyRegistration_WhenRequestLimitExceeded_returnBadRequest() throws IOException {
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
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void emailOnlyRegistration_WhenRequestHeaderInvalid_returnBadRequest() {
        // given
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(false);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"eec-secret-key\":\"x\"}");

        EmailList emailList = EmailList.builder().emails(null).build();

        // when
        ResponseEntity<List<EECUser>> res = accountsController.emailOnlyRegistration(emailList);

        // then
        Assert.assertEquals(res.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getUsers_GivenAValidListOfUID_ShouldReturnUserDetails() throws IOException {
        //setup
        List<UserDetails> userDetailsList = new ArrayList<>();
        userDetailsList.add(UserDetails.builder().uid(uids.get(0)).email(username).firstName(firstName).lastName(lastName).associatedAccounts(assoiciatedAccounts).build());
        userDetailsList.add(UserDetails.builder().uid(uids.get(1)).email(username).firstName(firstName).lastName(lastName).associatedAccounts(assoiciatedAccounts).build());
        userDetailsList.add(UserDetails.builder().uid(uids.get(2)).email(username).firstName(firstName).lastName(lastName).associatedAccounts(assoiciatedAccounts).build());
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(usersHandler.getUsers(uids)).thenReturn(userDetailsList);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        //execution
        ResponseEntity<List<UserDetails>> resp = accountsController.getUsers(uids);

        //validation
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.OK);

    }

    @Test
    public void getUsers_GivenAnEmptyListOfUID_ShouldReturnBadRequest() throws IOException {
        //setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(usersHandler.getUser(anyString())).thenReturn(null);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        //execution
        ResponseEntity<List<UserDetails>> resp = accountsController.getUsers(emptyUIDs);

        //validation
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.BAD_REQUEST);

    }

    @Test
    public void getUsers_GivenAnIOError_returnInternalServerError() throws IOException {
        //setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(usersHandler.getUsers(uids)).thenThrow(Exception.class);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        //execution
        ResponseEntity<List<UserDetails>> resp = accountsController.getUsers(uids);

        //validation
        Assert.assertEquals(resp.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);

    }


    @Test
    public void getUsers_GivenAnInvalidSHASignature_ShouldReturnBadRequest() {
        //setup
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(false);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"cdc-secret-key\":\"x\"}");

        //execution
        ResponseEntity<List<UserDetails>> resp = accountsController.getUsers(uids);

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

    @Test
    public void notifyRegistration_ifGivenAFederationUserUIDisSent_returnFederationAccount() {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        ResponseEntity<String> res = accountsController.notifyRegistration("Test", mockBody);

        //validation
        Assert.assertTrue(res.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void notifyRegistration_ifGivenANonFederationUserUIDisSent_returnError() {

        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(nonFederationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        ResponseEntity<String> res = accountsController.notifyRegistration("Test", mockBody);

        //validation
        Assert.assertEquals(res.getBody(), "The user was not created through federation.");
    }

    @Test
    public void notifyRegistration_ifConnectionIsLost_throwException() {
        //setup
        Mockito.when(hashValidationService.isValidHash(null, null)).thenReturn(true);

        //execution
        ResponseEntity<String> res = accountsController.notifyRegistration(null, null);

        //validation
        Assert.assertEquals(res.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void notifyRegistration_ifSNSNotificationFails_returnServiceUnavailable() {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(false);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        ResponseEntity<String> res = accountsController.notifyRegistration("Test", mockBody);

        //validation
        Assert.assertEquals(res.getStatusCode(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    public void notifyRegistration_ifNoEventsAreFound_returnError() {
        //setup
        String mockBody = "{\"events\":[]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        ResponseEntity<String> res = accountsController.notifyRegistration("Test", mockBody);

        //validation
        Assert.assertTrue(res.getStatusCode().is4xxClientError());
    }

    @Test
    public void notifyRegistration_ifGivenAnInvalidSignature_returnError() {
        //setup
        String mockBody = "{\"events\":[]}";
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(false);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        ResponseEntity<String> res = accountsController.notifyRegistration("Test", mockBody);

        //validation
        Assert.assertTrue(res.getStatusCode().is4xxClientError());
    }

    @Test
    public void notifyRegistration_ifNoUserIsFound_returnBadRequest() {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(null);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        ResponseEntity<String> res = accountsController.notifyRegistration("Test", mockBody);

        //validation
        Assert.assertTrue(res.getStatusCode().is4xxClientError());
    }

    @Test
    public void notifyRegistration_givenARegistrationOccurs_ThenNotificationServicePostRequestShouldBeCalled() throws IOException {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        String mockAccountToNotify = "Test Account";
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        Mockito.when(accountInfoHandler.prepareForProfileInfoNotification(any())).thenReturn(mockAccountToNotify);
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(AccountInfoUtils.getAccount());

        //execution
        accountsController.notifyRegistration("Test", mockBody);

        //validation
        Mockito.verify(notificationService).postRequest(any(), any());
    }

    @Test
    public void notifyRegistration_givenGNSPostRequestExecute_ShouldReceiveRequestResponse() throws IOException {
        //set up
        ReflectionTestUtils.setField(accountsController,"regNotificationUrl", "http://google.com");
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        String mockAccountToNotify = "Test Account";
        CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class, Mockito.RETURNS_DEEP_STUBS);
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream("".getBytes()));

        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(AccountInfoUtils.getAccount());
        Mockito.when(accountInfoHandler.prepareForProfileInfoNotification(any())).thenReturn(mockAccountToNotify);
        Mockito.when(mockResponse.getEntity()).thenReturn(entity);
        Mockito.when(mockResponse.getStatusLine().getStatusCode()).thenReturn(200);
        Mockito.when(notificationService.postRequest(anyString(), anyString())).thenReturn(mockResponse);
        doNothing().when(mockResponse).close();
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);

        //execution
        ResponseEntity response = accountsController.notifyRegistration("Test", mockBody);

        //validation
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void notifyRegistration_ifGivenAnIncorrectRegistrationType_returnError() {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountCreated\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(accountsService.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(snsHandler.sendSNSNotification(anyString())).thenReturn(true);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        ResponseEntity<String> res = accountsController.notifyRegistration("Test", mockBody);

        //validation
        Assert.assertEquals(res.getBody(), "the event type was not recognized");
    }
}
