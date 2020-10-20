package com.thermofisher.cdcam;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.gigya.socialize.GSResponse;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.enums.RegistrationType;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CDCValidationError;
import com.thermofisher.cdcam.model.HttpServiceResponse;
import com.thermofisher.cdcam.services.AccountRequestService;
import com.thermofisher.cdcam.services.CDCAccountsService;
import com.thermofisher.cdcam.services.HashValidationService;
import com.thermofisher.cdcam.services.HttpService;
import com.thermofisher.cdcam.utils.AccountInfoHandler;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.json.JSONException;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class AccountRequestServiceTests {
    private final List<String> uids = new ArrayList<>();
    private final String hashedString = "QJERFC2183DASJ=";

    @InjectMocks
    AccountRequestService accountRequestService;

    @Mock
    AccountInfoHandler accountInfoHandler;

    @Mock
    SNSHandler snsHandler;

    @Mock
    SecretsManager secretsManager;

    @Mock
    HashValidationService hashValidationService;

    @Mock
    CDCResponseHandler cdcResponseHandler;

    @Mock
    HttpService httpService;

    @Mock
    CDCAccountsService cdcAccountsService;

    private AccountInfo federationAccount = AccountInfo.builder().uid("0055").username("federatedUser@OIDC.com")
            .emailAddress("federatedUser@OIDC.com").firstName("first").lastName("last").country("country")
            .localeName("en_US").loginProvider("oidc").password("Randompassword1").regAttempts(0).city("testCity")
            .department("dep").company("myCompany").build();

    private AccountInfo nonFederationAccount = AccountInfo.builder().username("nonFederatedUser@email.com")
            .emailAddress("nonFederatedUser@email.com").firstName("first").lastName("last").country("country")
            .localeName("en_US").loginProvider("site").password("Randompassword1").regAttempts(0).city("testCity")
            .department("dep").company("myCompany").build();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        uids.add("001");
        uids.add("002");
        uids.add("003");
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn(hashedString);
    }

    @Test
    public void processRequest_IfGivenAUID_searchAccountInfo() throws JSONException, IOException{
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        Mockito.when(accountInfoHandler.prepareForGRPNotification(any())).thenCallRealMethod();
        Mockito.when(snsHandler.sendSNSNotification(anyString(),anyString())).thenReturn(true);

        //execution
        accountRequestService.processRequest("Test", mockBody);

        //validation
        Mockito.verify(cdcResponseHandler).getAccountInfo(any());
    }

    @Test
    public void processRequest_IfGivenAUID_setAwsQuickSightRole() throws JSONException, IOException{
        //setup
        AccountRequestService accountRequestServiceMock = Mockito.spy(accountRequestService);
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        Mockito.when(accountInfoHandler.prepareForGRPNotification(any())).thenCallRealMethod();
        Mockito.when(snsHandler.sendSNSNotification(anyString(),anyString())).thenReturn(true);
        Mockito.doNothing().when(accountRequestServiceMock).setAwsQuickSightRole(any());

        //execution
        accountRequestServiceMock.processRequest("Test", mockBody);

        //validation
        Mockito.verify(accountRequestServiceMock).setAwsQuickSightRole(any());
    }

    @Test
    public void processRequest_IfGivenAFederatedUser_searchForDuplicatedAccountsInCDC() throws JSONException, IOException {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"0055\"}}]}";
        Mockito.when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(cdcResponseHandler.disableAccount(anyString())).thenReturn(true);
        Mockito.when(accountInfoHandler.prepareForGRPNotification(any())).thenCallRealMethod();
        Mockito.when(snsHandler.sendSNSNotification(anyString(),anyString())).thenReturn(true);

        //execution
        accountRequestService.processRequest("Federated account", mockBody);

        //validation
        Mockito.verify(cdcResponseHandler).searchDuplicatedAccountUid(federationAccount.getUid(),federationAccount.getEmailAddress());
    }

    @Test
    public void processRequest_IfGivenAFederatedUser_disableDuplicatedAccounts() throws JSONException, IOException {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"0055\"}}]}";
        Mockito.when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(cdcResponseHandler.searchDuplicatedAccountUid(anyString(), anyString())).thenReturn("0055");
        Mockito.when(accountInfoHandler.prepareForGRPNotification(any())).thenCallRealMethod();
        Mockito.when(snsHandler.sendSNSNotification(anyString(),anyString())).thenReturn(true);

        //execution
        accountRequestService.processRequest("Federated account", mockBody);

        //validation
        Mockito.verify(cdcResponseHandler).disableAccount(anyString());
    }

    @Test
    public void processRequest_IfGivenAFederatedUser_saveDuplicatedAccountUidToAccount() throws JSONException, IOException {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"0055\"}}]}";

        Mockito.when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(cdcResponseHandler.searchDuplicatedAccountUid(anyString(), anyString())).thenReturn("0055");
        Mockito.when(cdcResponseHandler.disableAccount(anyString())).thenReturn(true);
        Mockito.when(accountInfoHandler.prepareForGRPNotification(any())).thenCallRealMethod();
        Mockito.when(snsHandler.sendSNSNotification(anyString(),anyString())).thenReturn(true);

        //execution
        accountRequestService.processRequest("Federated account", mockBody);

        //validation
        federationAccount.setDuplicatedAccountUid("0055");
    }


    @Test
    public void processRequest_IfValidHashIsFalse_thenLogError() throws JSONException {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(nonFederationAccount);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(false);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        accountRequestService.processRequest("Test", mockBody);
    }

    @Test
    public void processRequest_IfEventTypeIsNotRegistration_thenLogError() throws JSONException {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"undefined\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(nonFederationAccount);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        accountRequestService.processRequest("Test", mockBody);
    }

    @Test
    public void processRequest_IfRawBodyHasNoEvents_thenLogError() throws JSONException {
        //setup
        String mockBody = "{\"events\":[]}";
        Mockito.when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(nonFederationAccount);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        accountRequestService.processRequest("Test", mockBody);
    }

    @Test
    public void processRequest_IfAccountIsNull_thenLogError() throws JSONException {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(null);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        accountRequestService.processRequest("Test", mockBody);
    }

    @Test
    public void processRequest_IfGivenAnInvalidUid_thenCatchException() throws JSONException {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        Mockito.when(cdcResponseHandler.getAccountInfo(anyString())).thenThrow(Exception.class);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\"`:\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");

        //execution
        accountRequestService.processRequest("Test", mockBody);
    }

    @Test
    public void processRequest_IfGivenAccountToNotify_ThenSendNotification() throws JSONException, IOException {
        //setup
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        String mockAccountToNotify = "Test Account";
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        Mockito.when(accountInfoHandler.prepareForProfileInfoNotification(any())).thenReturn(mockAccountToNotify);
        Mockito.when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(AccountUtils.getFederatedAccount());

        //execution
        accountRequestService.processRequest("Test", mockBody);

        //validation
        Mockito.verify(snsHandler,atLeastOnce()).sendSNSNotification(any(), any());
    }

    @Test
    public void processRequest_IfGivenAccount_ThenSendNotificationToGRP() throws JSONException, IOException{
        //setup
        ReflectionTestUtils.setField(accountRequestService,"snsRegistrationTopic","regSNS");
        ReflectionTestUtils.setField(accountRequestService,"snsAccountInfoTopic","infoSNS");
        String mockBody = "{\"events\":[{\"type\":\"accountRegistered\",\"data\":{\"uid\":\"00000\"}}]}";
        String mockAccountToNotify = "Test Account";
        CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class, Mockito.RETURNS_DEEP_STUBS);
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream("".getBytes()));

        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenReturn(true);
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenReturn("Test");
        Mockito.when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(AccountUtils.getFederatedAccount());
        Mockito.when(accountInfoHandler.prepareForProfileInfoNotification(any())).thenReturn(mockAccountToNotify);
        Mockito.when(mockResponse.getEntity()).thenReturn(entity);
        Mockito.when(mockResponse.getStatusLine().getStatusCode()).thenReturn(200);

        doNothing().when(mockResponse).close();
        Mockito.when(accountInfoHandler.prepareForGRPNotification(any())).thenCallRealMethod();
        Mockito.when(snsHandler.sendSNSNotification(anyString(),anyString())).thenReturn(true);

        //execution
        accountRequestService.processRequest("Test", mockBody);

        //validation
        Mockito.verify(snsHandler,atLeastOnce()).sendSNSNotification(anyString(),anyString());
    }

    @Test
    public void processRegistrationRequest_givenANullAccount_returnNull(){
        // when
        CDCResponseData responseData = accountRequestService.processRegistrationRequest(null);

        // then
        Assert.assertNull(responseData);
    }

    @Test
    public void processRegistrationRequest_givenAValidAccount_returnCDCResponseData() throws IOException {
        // given
        AccountInfo accountInfo = AccountInfo.builder()
                .username("test")
                .emailAddress("email")
                .firstName("first")
                .lastName("last")
                .password("test")
                .build();

        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID("9f6f2133e57144d787574d49c0b9908e");
        cdcResponseData.setStatusCode(0);
        cdcResponseData.setStatusReason("");
        Mockito.when(cdcResponseHandler.register(any())).thenReturn(cdcResponseData);
        Mockito.when(accountInfoHandler.prepareProfileForRegistration(any())).thenCallRealMethod();

        // when
        accountRequestService.processRegistrationRequest(accountInfo);

        // then
        Mockito.verify(cdcResponseHandler).register(any());
    }

    @Test
    public void processRegistrationRequest_givenAnInvalidAccount_returnCDCResponseData() throws IOException {
        // given
        AccountInfo accountInfo = AccountInfo.builder()
            .username("test")
            .emailAddress("email")
            .firstName("first")
            .lastName("last")
            .password("1")
            .build();

        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setStatusCode(400);
        cdcResponseData.setStatusReason("");
        List<CDCValidationError> errors = new ArrayList<>();
        CDCValidationError error = new CDCValidationError();
        error.setErrorCode(400);
        error.setFieldName("password");
        error.setMessage("incorrect password");
        errors.add(error);
        cdcResponseData.setValidationErrors(errors);

        Mockito.when(cdcResponseHandler.register(any())).thenReturn(cdcResponseData);
        Mockito.when(accountInfoHandler.prepareProfileForRegistration(any())).thenCallRealMethod();

        // when
        accountRequestService.processRegistrationRequest(accountInfo);

        // then
        Mockito.verify(cdcResponseHandler).register(any());
        Mockito.verify(snsHandler, never()).sendSNSNotification(any(),any());
    }

    @Test
    public void processRegistrationRequest_givenAnInvalidEmail_returnCDCResponseData() throws IOException {
        // given
        AccountInfo accountInfo = AccountInfo.builder()
                .username("test")
                .emailAddress("email")
                .firstName("first")
                .lastName("last")
                .password("1")
                .build();

        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setStatusCode(400);
        cdcResponseData.setStatusReason("");

        Mockito.when(cdcResponseHandler.register(any())).thenReturn(cdcResponseData);
        Mockito.when(accountInfoHandler.prepareProfileForRegistration(any())).thenCallRealMethod();

        // when
        accountRequestService.processRegistrationRequest(accountInfo);

        // then
        Mockito.verify(cdcResponseHandler).register(any());
        Mockito.verify(snsHandler, never()).sendSNSNotification(any(),any());
    }

    @Test
    public void sendConfirmationEmail_givenAccountWithValidFormat_thenConfirmationEmailPostRequestShouldBeMade() throws IOException {
        StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
        HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse mockHttpCloseableResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpServiceResponse mockHttpResponse = HttpServiceResponse.builder()
                .closeableHttpResponse(mockHttpCloseableResponse)
                .build();

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(200);
        Mockito.when(mockHttpResponse.getCloseableHttpResponse().getStatusLine()).thenReturn(mockStatusLine);
        Mockito.when(mockHttpResponse.getCloseableHttpResponse().getEntity()).thenReturn(mockEntity);
        Mockito.when(httpService.post(any(), any())).thenReturn(mockHttpResponse);

        AccountInfo accountInfo = AccountInfo.builder()
                .username("test")
                .emailAddress("email")
                .firstName("first")
                .lastName("last")
                .password("1")
                .localeName("en_US")
                .registrationType(RegistrationType.BASIC.getValue())
                .build();

        accountRequestService.sendConfirmationEmail(accountInfo);

        verify(httpService, times(1)).post(any(), any());
    }

    @Test
    public void sendConfirmationEmail_givenConfirmationEmailPostRequestReturnsDifferentThan200_noExceptionShouldOccur() throws IOException {
        StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
        HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse mockHttpCloseableResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpServiceResponse mockHttpResponse = HttpServiceResponse.builder()
                .closeableHttpResponse(mockHttpCloseableResponse)
                .build();

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(500);
        Mockito.when(mockHttpResponse.getCloseableHttpResponse().getStatusLine()).thenReturn(mockStatusLine);
        Mockito.when(mockHttpResponse.getCloseableHttpResponse().getEntity()).thenReturn(mockEntity);
        Mockito.when(httpService.post(any(), any())).thenReturn(mockHttpResponse);

        AccountInfo accountInfo = AccountInfo.builder()
                .username("test")
                .emailAddress("email")
                .firstName("first")
                .lastName("last")
                .password("1")
                .localeName("en_US")
                .registrationType(RegistrationType.BASIC.getValue())
                .build();

        accountRequestService.sendConfirmationEmail(accountInfo);

        verify(httpService, times(1)).post(any(), any());
    }

    @Test(expected = IOException.class)
    public void sendConfirmationEmail_givenConfirmationEmailPostRequestFails_ExceptionShouldBeThrown() throws IOException {
        CloseableHttpResponse mockHttpCloseableResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpServiceResponse mockHttpResponse = HttpServiceResponse.builder()
                .closeableHttpResponse(mockHttpCloseableResponse)
                .build();

        Mockito.when(mockHttpResponse.getCloseableHttpResponse().getEntity()).thenReturn(null);
        Mockito.when(httpService.post(any(), any())).thenReturn(mockHttpResponse);

        AccountInfo accountInfo = AccountInfo.builder()
                .username("test")
                .emailAddress("email")
                .firstName("first")
                .lastName("last")
                .password("1")
                .localeName("en_US")
                .registrationType(RegistrationType.BASIC.getValue())
                .build();

        accountRequestService.sendConfirmationEmail(accountInfo);
    }

    @Test
    public void sendVerificationEmailSync_triggerVerificationEmailProcess_givenRequestIsSuccessful_whenTriggered_ReturnResponse() throws IOException {
        // setup
        HttpStatus mockStatus = HttpStatus.OK;
        CDCResponseData mockResponse = Mockito.mock(CDCResponseData.class);

        when(mockResponse.getStatusCode()).thenReturn(mockStatus.value());
        when(cdcResponseHandler.sendVerificationEmail(any())).thenReturn(mockResponse);

        // execution
        CDCResponseData response = accountRequestService.sendVerificationEmailSync("test");

        // validation
        Assert.assertEquals(response.getStatusCode(), mockStatus.value());
    }

    @Test
    public void sendVerificationEmailSync_triggerVerificationEmailProcess_givenRequestIsNotSuccessful_whenTriggered_ReturnResponse() throws IOException {
        // setup
        HttpStatus mockStatus = HttpStatus.BAD_REQUEST;
        CDCResponseData mockResponse = Mockito.mock(CDCResponseData.class);

        when(mockResponse.getStatusCode()).thenReturn(mockStatus.value());
        when(cdcResponseHandler.sendVerificationEmail(any())).thenReturn(mockResponse);

        // execution
        CDCResponseData response = accountRequestService.sendVerificationEmailSync("test");

        // validation
        Assert.assertEquals(response.getStatusCode(), mockStatus.value());
    }

    @Test
    public void sendVerificationEmailSync_triggerVerificationEmailProcess_givenExceptionOccurss_whenTriggered_ReturnInternalServerErrorResponse() throws IOException {
        // setup
        when(cdcResponseHandler.sendVerificationEmail(any())).thenThrow(IOException.class);

        // execution
        CDCResponseData response = accountRequestService.sendVerificationEmailSync("test");

        // validation
        Assert.assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void sendVerificationEmail_triggerVerificationEmailProcess_whenTriggered_sendVerificationProccessShouldBeCalled() throws IOException {
        // setup
        String uid = "abc123";
        HttpStatus mockStatus = HttpStatus.BAD_REQUEST;
        CDCResponseData mockResponse = Mockito.mock(CDCResponseData.class);

        when(mockResponse.getStatusCode()).thenReturn(mockStatus.value());
        when(cdcResponseHandler.sendVerificationEmail(uid)).thenReturn(mockResponse);

        // execution
        accountRequestService.sendVerificationEmail(uid);

        // validation
        verify(cdcResponseHandler, times(1)).sendVerificationEmail(uid);
    }

    @Test
    public void setAwsQuickSightRole_givenAUID_should_setUserInfo() throws JSONException {
        //setup
        String uid = UUID.randomUUID().toString();
        String mockData = "{\"awsQuickSightRole\":\"Test\"}";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        Mockito.when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        Mockito.when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        Mockito.when(cdcAccountsService.setUserInfo(any(),any(),any())).thenReturn(mockCdcResponse);

        //execution
        accountRequestService.setAwsQuickSightRole(uid);

        //validation
        Mockito.verify(cdcAccountsService).setUserInfo(uid,mockData,"");

    }
}
