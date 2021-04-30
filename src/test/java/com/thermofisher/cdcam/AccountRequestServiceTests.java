package com.thermofisher.cdcam;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.thermofisher.cdcam.model.HttpServiceResponse;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CDCValidationError;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.notifications.MergedAccountNotification;
import com.thermofisher.cdcam.services.AccountRequestService;
import com.thermofisher.cdcam.services.CDCAccountsService;
import com.thermofisher.cdcam.services.HttpService;
import com.thermofisher.cdcam.services.NotificationService;
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
import org.mockito.MockedStatic;
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

    @InjectMocks
    AccountRequestService accountRequestService;

    @Mock
    AccountInfoHandler accountInfoHandler;

    @Mock
    CDCAccountsService cdcAccountsService;

    @Mock
    CDCResponseHandler cdcResponseHandler;

    @Mock
    HttpService httpService;

    @Mock
    NotificationService notificationService;

    @Mock
    SNSHandler snsHandler;

    @Mock
    SecretsManager secretsManager;

    private AccountInfo federationAccount = AccountInfo.builder().uid("0055").username("federatedUser@OIDC.com")
            .emailAddress("federatedUser@OIDC.com").firstName("first").lastName("last").country("country")
            .localeName("en_US").loginProvider("oidc").password("Randompassword1").regAttempts(0).city("testCity")
            .company("myCompany").build();

    private AccountInfo nonFederationAccount = AccountInfo.builder().username("nonFederatedUser@email.com")
            .emailAddress("nonFederatedUser@email.com").firstName("first").lastName("last").country("country")
            .localeName("en_US").loginProvider("site").password("Randompassword1").regAttempts(0).city("testCity")
            .company("myCompany").build();

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        uids.add("001");
        uids.add("002");
        uids.add("003");
    }

    @Test
    public void onAccountRegistered_IfGivenAUID_searchAccountInfo() throws IOException, CustomGigyaErrorException{
        // given
        String uid = UUID.randomUUID().toString();
        when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(federationAccount);
        when(accountInfoHandler.buildRegistrationNotificationPayload(any())).thenCallRealMethod();
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        accountRequestService.onAccountRegistered(uid);

        // then
        Mockito.verify(cdcResponseHandler).getAccountInfo(any());
    }

    @Test
    public void onAccountRegistered_IfGivenAUID_setAwsQuickSightRole() throws IOException, CustomGigyaErrorException{
        // given
        AccountRequestService accountRequestServiceMock = Mockito.spy(accountRequestService);
        String uid = UUID.randomUUID().toString();
        when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(federationAccount);
        when(accountInfoHandler.buildRegistrationNotificationPayload(any())).thenCallRealMethod();
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());
        Mockito.doNothing().when(accountRequestServiceMock).setAwsQuickSightRole(any());

        // when
        accountRequestServiceMock.onAccountRegistered(uid);

        // then
        Mockito.verify(accountRequestServiceMock).setAwsQuickSightRole(any());
    }

    @Test
    public void onAccountRegistered_IfAccountIsNull_thenLogError() throws CustomGigyaErrorException {
        // given
        String uid = UUID.randomUUID().toString();
        when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(null);

        // when
        accountRequestService.onAccountRegistered(uid);
    }

    @Test
    public void onAccountRegistered_IfGivenAccountToNotify_ThenSendNotification() throws IOException, CustomGigyaErrorException {
        // given
        String uid = UUID.randomUUID().toString();
        String mockAccountToNotify = "Test Account";
        when(accountInfoHandler.prepareForProfileInfoNotification(any())).thenReturn(mockAccountToNotify);
        when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(AccountUtils.getFederatedAccount());

        // when
        accountRequestService.onAccountRegistered(uid);

        // then
        Mockito.verify(snsHandler,atLeastOnce()).sendNotification(any(), any(), any());
    }

    @Test
    public void onAccountRegistered_IfGivenAccount_ThenSendNotificationToGRP() throws IOException, CustomGigyaErrorException {
        // given
        ReflectionTestUtils.setField(accountRequestService,"snsRegistrationTopic","regSNS");
        ReflectionTestUtils.setField(accountRequestService,"snsAccountInfoTopic","infoSNS");
        String uid = UUID.randomUUID().toString();
        String mockAccountToNotify = "Test Account";
        CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class, Mockito.RETURNS_DEEP_STUBS);
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream("".getBytes()));

        when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(AccountUtils.getFederatedAccount());
        when(accountInfoHandler.prepareForProfileInfoNotification(any())).thenReturn(mockAccountToNotify);
        when(mockResponse.getEntity()).thenReturn(entity);
        when(mockResponse.getStatusLine().getStatusCode()).thenReturn(200);
        doNothing().when(mockResponse).close();
        when(accountInfoHandler.buildRegistrationNotificationPayload(any())).thenCallRealMethod();
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        // when
        accountRequestService.onAccountRegistered(uid);

        // then
        Mockito.verify(snsHandler,atLeastOnce()).sendNotification(anyString(),anyString());
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
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setEmailAddress("invalid-email");

        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID("9f6f2133e57144d787574d49c0b9908e");
        cdcResponseData.setStatusCode(0);
        cdcResponseData.setStatusReason("");
        when(cdcResponseHandler.register(any())).thenReturn(cdcResponseData);

        // when
        accountRequestService.processRegistrationRequest(accountInfo);

        // then
        Mockito.verify(cdcResponseHandler).register(any());
    }

    @Test
    public void processRegistrationRequest_givenAnInvalidAccount_returnCDCResponseData() throws IOException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setEmailAddress("invalid-email");

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

        when(cdcResponseHandler.register(any())).thenReturn(cdcResponseData);

        // when
        accountRequestService.processRegistrationRequest(accountInfo);

        // then
        Mockito.verify(cdcResponseHandler).register(any());
        Mockito.verify(snsHandler, never()).sendNotification(any(),any());
    }

    @Test
    public void processRegistrationRequest_givenAnInvalidEmail_returnCDCResponseData() throws IOException {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        accountInfo.setEmailAddress("invalid-email");

        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setStatusCode(400);
        cdcResponseData.setStatusReason("");

        when(cdcResponseHandler.register(any())).thenReturn(cdcResponseData);

        // when
        accountRequestService.processRegistrationRequest(accountInfo);

        // then
        Mockito.verify(cdcResponseHandler).register(any());
        Mockito.verify(snsHandler, never()).sendNotification(any(),any());
    }

    @Test
    public void sendConfirmationEmail_givenAccountWithValidFormat_thenConfirmationEmailPostRequestShouldBeMade() throws IOException {
        StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
        HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse mockHttpCloseableResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpServiceResponse mockHttpResponse = HttpServiceResponse.builder()
                .closeableHttpResponse(mockHttpCloseableResponse)
                .build();

        when(mockStatusLine.getStatusCode()).thenReturn(200);
        when(mockHttpResponse.getCloseableHttpResponse().getStatusLine()).thenReturn(mockStatusLine);
        when(mockHttpResponse.getCloseableHttpResponse().getEntity()).thenReturn(mockEntity);
        when(httpService.post(any(), any())).thenReturn(mockHttpResponse);

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

        when(mockStatusLine.getStatusCode()).thenReturn(500);
        when(mockHttpResponse.getCloseableHttpResponse().getStatusLine()).thenReturn(mockStatusLine);
        when(mockHttpResponse.getCloseableHttpResponse().getEntity()).thenReturn(mockEntity);
        when(httpService.post(any(), any())).thenReturn(mockHttpResponse);

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

        when(mockHttpResponse.getCloseableHttpResponse().getEntity()).thenReturn(null);
        when(httpService.post(any(), any())).thenReturn(mockHttpResponse);

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
    public void sendVerificationEmailSync_triggerVerificationEmailProcess_givenExceptionOccurs_whenTriggered_ReturnInternalServerErrorResponse() throws IOException {
        // setup
        when(cdcResponseHandler.sendVerificationEmail(any())).thenThrow(IOException.class);

        // execution
        CDCResponseData response = accountRequestService.sendVerificationEmailSync("test");

        // validation
        Assert.assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void sendVerificationEmail_triggerVerificationEmailProcess_whenTriggered_sendVerificationProcessShouldBeCalled() throws IOException {
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
        // given
        String uid = UUID.randomUUID().toString();
        String mockData = "{\"awsQuickSightRole\":\"Test\"}";
        GSResponse mockCdcResponse = Mockito.mock(GSResponse.class);
        when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        when(secretsManager.getProperty(any(), anyString())).thenReturn("Test");
        when(cdcAccountsService.setUserInfo(any(),any(),any())).thenReturn(mockCdcResponse);

        // when
        accountRequestService.setAwsQuickSightRole(uid);

        // then
        Mockito.verify(cdcAccountsService).setUserInfo(uid, mockData, "");
    }

    @Test(expected = NullPointerException.class)
    public void givenOnAccountMergedIsCalled_WhenUidParameterIsNull_ThenNullPointerExceptionShouldBeThrown() {
        // when
        accountRequestService.onAccountMerged(null);
    }

    @Test
    public void givenOnAccountMergedIsCalled_WhenAccountIsFederated_ThenAccountMergedNotificationShouldBeSent() throws CustomGigyaErrorException {
        // when
        String uid = AccountUtils.uid;
        AccountInfo accountMock = AccountUtils.getFederatedAccount();
        MergedAccountNotification mergedAccountNotification = MergedAccountNotification.buildFrom(accountMock);
        when(cdcResponseHandler.getAccountInfo(uid)).thenReturn(accountMock);
        doNothing().when(notificationService).sendAccountMergedNotification(any());

        try (MockedStatic<MergedAccountNotification> mergedAccountNotificationStatic = Mockito.mockStatic(MergedAccountNotification.class)) {
            // when
            mergedAccountNotificationStatic.when(() -> MergedAccountNotification.buildFrom(any())).thenReturn(mergedAccountNotification);
            accountRequestService.onAccountMerged(uid);

            // then
            verify(notificationService).sendAccountMergedNotification(mergedAccountNotification);
        }
    }

    @Test
    public void givenOnAccountMergedIsCalled_WhenAccountIsNotFederated_ThenAccountMergedNotificationShouldNotBeSent() throws CustomGigyaErrorException {
        // when
        String uid = AccountUtils.uid;
        AccountInfo accountMock = AccountUtils.getSiteAccount();
        MergedAccountNotification mergedAccountNotification = MergedAccountNotification.buildFrom(accountMock);
        when(cdcResponseHandler.getAccountInfo(uid)).thenReturn(accountMock);
        doNothing().when(notificationService).sendAccountMergedNotification(any());

        try (MockedStatic<MergedAccountNotification> mergedAccountNotificationStatic = Mockito.mockStatic(MergedAccountNotification.class)) {
            // when
            mergedAccountNotificationStatic.when(() -> MergedAccountNotification.buildFrom(any())).thenReturn(mergedAccountNotification);
            accountRequestService.onAccountMerged(uid);

            // then
            verify(notificationService, never()).sendAccountMergedNotification(any());
        }
    }

    @Test
    public void onAccountRegistered_IfGivenAFederatedUser_searchForDuplicatedAccountsInCDC() throws IOException, CustomGigyaErrorException {
        //setup
        String uid = UUID.randomUUID().toString();
        Mockito.when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(cdcResponseHandler.disableAccount(anyString())).thenReturn(true);
        Mockito.when(accountInfoHandler.buildRegistrationNotificationPayload(any())).thenCallRealMethod();
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        //execution
        accountRequestService.onAccountRegistered(uid);

        //validation
        Mockito.verify(cdcResponseHandler).searchDuplicatedAccountUid(federationAccount.getUid(),federationAccount.getEmailAddress());
    }

    @Test
    public void onAccountRegistered_IfGivenAFederatedUser_disableDuplicatedAccounts() throws IOException, CustomGigyaErrorException {
        //setup
        String uid = UUID.randomUUID().toString();
        Mockito.when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(cdcResponseHandler.searchDuplicatedAccountUid(anyString(), anyString())).thenReturn("0055");
        Mockito.when(accountInfoHandler.buildRegistrationNotificationPayload(any())).thenCallRealMethod();
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        //execution
        accountRequestService.onAccountRegistered(uid);

        //validation
        Mockito.verify(cdcResponseHandler).disableAccount(anyString());
    }

    @Test
    public void onAccountRegistered_IfGivenAFederatedUser_saveDuplicatedAccountUidToAccount() throws IOException, CustomGigyaErrorException {
        //setup
        String uid = UUID.randomUUID().toString();

        Mockito.when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(federationAccount);
        Mockito.when(cdcResponseHandler.searchDuplicatedAccountUid(anyString(), anyString())).thenReturn("0055");
        Mockito.when(cdcResponseHandler.disableAccount(anyString())).thenReturn(true);
        Mockito.when(accountInfoHandler.buildRegistrationNotificationPayload(any())).thenCallRealMethod();
        doNothing().when(snsHandler).sendNotification(anyString(), anyString());

        //execution
        accountRequestService.onAccountRegistered(uid);

        //validation
        federationAccount.setDuplicatedAccountUid("0055");
    }
}
