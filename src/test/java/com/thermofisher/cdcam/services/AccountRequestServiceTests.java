package com.thermofisher.cdcam.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gigya.socialize.GSKeyNotFoundException;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccountV2;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CDCValidationError;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.OpenIdProvider;
import com.thermofisher.cdcam.model.cdc.OpenIdRelyingParty;
import com.thermofisher.cdcam.model.notifications.MergedAccountNotification;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class AccountRequestServiceTests {
    private final List<String> uids = new ArrayList<>();

    @InjectMocks
    AccountRequestService accountRequestService;

    @Mock
    CDCAccountsService cdcAccountsService;

    @Mock
    CDCResponseHandler cdcResponseHandler;

    @Mock
    NotificationService notificationService;

    @Mock
    SNSHandler snsHandler;

    @Mock
    SecretsService secretsService;

    @Captor
    ArgumentCaptor<CDCAccount> cdcAccountCaptor;

    private AccountInfo federationAccount;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        uids.add("001");
        uids.add("002");
        uids.add("003");
        federationAccount = AccountInfo.builder()
            .uid("0055")
            .username("federatedUser@OIDC.com")
            .emailAddress("federatedUser@OIDC.com")
            .firstName("first")
            .lastName("last")
            .country("country")    
            .localeName("en_US")
            .loginProvider("oidc")
            .password("Randompassword1")
            .regAttempts(0)
            .city("testCity")
            .company("myCompany")
            .build();
    }

    @Test
    public void onAccountRegistered_GivenUIDisValid_ThenGetAccountInfo() throws IOException, CustomGigyaErrorException{
        // given
        String uid = UUID.randomUUID().toString();
        when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(federationAccount);

        // when
        accountRequestService.onAccountRegistered(uid);

        // then
        verify(cdcResponseHandler).getAccountInfo(any());
    }

    @Test
    public void onAccountRegistered_ThenSaveAWSQuickSightRole() throws IOException, CustomGigyaErrorException, JSONException{
        // given
        String uid = UUID.randomUUID().toString();
        String mockQuickSightRole = RandomStringUtils.random(10);
        when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(federationAccount);
        when(secretsService.get(anyString())).thenReturn(mockQuickSightRole);
        doNothing().when(cdcResponseHandler).setAccountInfo(any());

        // when
        accountRequestService.onAccountRegistered(uid);

        // then
        verify(cdcResponseHandler).setAccountInfo(cdcAccountCaptor.capture());
        CDCAccount capturedCdcAccount = cdcAccountCaptor.getValue();
        assertEquals(mockQuickSightRole, capturedCdcAccount.getData().getAwsQuickSightRole());
    }

    @Test
    public void onAccountRegistered_ThenSaveOpenIdProviderDescription() throws IOException, CustomGigyaErrorException, JSONException, GSKeyNotFoundException{
        // given
        String uid = UUID.randomUUID().toString();
        
        String providerClientId = RandomStringUtils.random(10);
        String providerDescriptionMock = RandomStringUtils.random(10);
        OpenIdRelyingParty rpMock = OpenIdRelyingParty.builder().clientId(providerClientId).description(providerDescriptionMock).build();
        when(cdcResponseHandler.getRP(anyString())).thenReturn(rpMock);

        federationAccount.setOpenIdProviderId(providerClientId);
        when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(federationAccount);
        doNothing().when(cdcResponseHandler).setAccountInfo(any());
        
        // when
        accountRequestService.onAccountRegistered(uid);

        // then
        verify(cdcResponseHandler).setAccountInfo(cdcAccountCaptor.capture());
        CDCAccount capturedCdcAccount = cdcAccountCaptor.getValue();
        String providerDescriptionResult = capturedCdcAccount.getData()
            .getRegistration()
            .getOpenIdProvider()
            .getProviderName();
        assertEquals(providerDescriptionMock, providerDescriptionResult);
    }

    @Test
    public void onAccountRegistered_GivenAccountDoesntHaveProvider_ThenShouldNotFetchRPData_AndSavedProviderShouldBeNull() throws IOException, CustomGigyaErrorException, JSONException, GSKeyNotFoundException{
        // given
        String uid = UUID.randomUUID().toString();
        when(cdcResponseHandler.getRP(anyString())).thenCallRealMethod();

        when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(federationAccount);
        doNothing().when(cdcResponseHandler).setAccountInfo(any());
        
        // when
        accountRequestService.onAccountRegistered(uid);

        // then
        verify(cdcResponseHandler, never()).getRP(anyString());
        verify(cdcResponseHandler).setAccountInfo(cdcAccountCaptor.capture());
        CDCAccount capturedCdcAccount = cdcAccountCaptor.getValue();
        OpenIdProvider openIdProviderResult = capturedCdcAccount.getData()
            .getRegistration()
            .getOpenIdProvider();
        assertNull(openIdProviderResult);
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
    public void onAccountRegistered_GivenNewAccountRegistered_ThenSendNotifyAccountInfoNotification() throws IOException, CustomGigyaErrorException {
        // given
        ReflectionTestUtils.setField(accountRequestService, "cipdc", "us");
        String uid = UUID.randomUUID().toString();
        when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(AccountUtils.getFederatedAccount());
        doNothing().when(notificationService).sendNotifyAccountInfoNotification(any(), anyString());

        // when
        accountRequestService.onAccountRegistered(uid);

        // then
        verify(notificationService).sendNotifyAccountInfoNotification(any(), anyString());
    }

    @Test
    public void onAccountRegistered_GivenJsonProcessingExceptionIsThrown_ThenDoNotSendNotifyAccountInfoNotification() throws IOException, CustomGigyaErrorException {
        // given
        ReflectionTestUtils.setField(accountRequestService, "cipdc", "us");
        String uid = UUID.randomUUID().toString();
        when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(AccountUtils.getFederatedAccount());
        doThrow(JsonProcessingException.class).when(notificationService).sendNotifyAccountInfoNotification(any(), anyString());

        // when
        accountRequestService.onAccountRegistered(uid);

        // then
        verify(notificationService).sendNotifyAccountInfoNotification(any(), anyString());
    }

    @Test
    public void onAccountRegistered_GivenNewAccountRegistered_ThenSendAccountRegistrationNotification() throws IOException, CustomGigyaErrorException {
        // given
        ReflectionTestUtils.setField(accountRequestService, "cipdc", "us");
        String uid = UUID.randomUUID().toString();
        when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(AccountUtils.getFederatedAccount());
        doNothing().when(notificationService).sendAccountRegisteredNotification(any(), anyString());

        // when
        accountRequestService.onAccountRegistered(uid);

        // then
        verify(notificationService).sendAccountRegisteredNotification(any(), anyString());
    }

    @Test(expected = NullPointerException.class)
    public void processRegistrationRequest_givenANullAccount_ThrowNullPointerException() throws NoSuchAlgorithmException, JSONException, IOException, CustomGigyaErrorException{
        // when
        accountRequestService.createAccount(null);
    }

    @Test
    public void processRegistrationRequest_givenAValidAccount_returnCDCResponseData() throws IOException, NoSuchAlgorithmException, JSONException, CustomGigyaErrorException {
        // given
        ReflectionTestUtils.setField(cdcResponseHandler, "isNewMarketingConsentEnabled", false);
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID("9f6f2133e57144d787574d49c0b9908e");
        cdcResponseData.setStatusCode(200);
        when(cdcResponseHandler.register(any(CDCNewAccount.class))).thenReturn(cdcResponseData);

        // when
        accountRequestService.createAccount(accountInfo);

        // then
        verify(cdcResponseHandler).register(any(CDCNewAccount.class));
    }

    @Test
    public void processRegistrationRequest_givenAValidAccount_returnCDCResponseData_V2() throws IOException, NoSuchAlgorithmException, JSONException, CustomGigyaErrorException {
        // given
        ReflectionTestUtils.setField(accountRequestService, "isNewMarketingConsentEnabled", true);
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID("9f6f2133e57144d787574d49c0b9908e");
        cdcResponseData.setStatusCode(200);
        when(cdcResponseHandler.register(any(CDCNewAccountV2.class))).thenReturn(cdcResponseData);

        // when
        accountRequestService.createAccount(accountInfo);

        // then
        verify(cdcResponseHandler).register(any(CDCNewAccountV2.class));
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void processRegistrationRequest_GivenCDCReturnsAnErrorResponse_ThenThrowCustomGigyaErrorException() throws IOException, NoSuchAlgorithmException, JSONException, CustomGigyaErrorException {
        // given
        ReflectionTestUtils.setField(accountRequestService, "isNewMarketingConsentEnabled", false);
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
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
        when(cdcResponseHandler.register(any(CDCNewAccount.class))).thenReturn(cdcResponseData);

        // when
        accountRequestService.createAccount(accountInfo);
    }

    @Test(expected = CustomGigyaErrorException.class)
    public void processRegistrationRequest_GivenCDCReturnsAnErrorResponse_ThenThrowCustomGigyaErrorException_V2() throws IOException, NoSuchAlgorithmException, JSONException, CustomGigyaErrorException {
        // given
        ReflectionTestUtils.setField(accountRequestService, "isNewMarketingConsentEnabled", true);
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
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
        when(cdcResponseHandler.register(any(CDCNewAccountV2.class))).thenReturn(cdcResponseData);

        // when
        accountRequestService.createAccount(accountInfo);
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
        MergedAccountNotification mergedAccountNotification = MergedAccountNotification.build(accountMock);
        when(cdcResponseHandler.getAccountInfo(uid)).thenReturn(accountMock);
        doNothing().when(notificationService).sendAccountMergedNotification(any());

        try (MockedStatic<MergedAccountNotification> mergedAccountNotificationStatic = Mockito.mockStatic(MergedAccountNotification.class)) {
            // when
            mergedAccountNotificationStatic.when(() -> MergedAccountNotification.build(any())).thenReturn(mergedAccountNotification);
            accountRequestService.onAccountMerged(uid);

            // then
            verify(notificationService).sendAccountMergedNotification(mergedAccountNotification);
        }
    }

    @Test
    public void givenOnAccountMergedIsCalled_WhenCustomGigyaErrorExceptionIsThrown_ThenAccountMergedNotificationShouldNotBeSent() throws CustomGigyaErrorException {
        // given
        String uid = AccountUtils.uid;
        AccountInfo accountMock = AccountUtils.getFederatedAccount();
        MergedAccountNotification mergedAccountNotification = MergedAccountNotification.build(accountMock);
        when(cdcResponseHandler.getAccountInfo(uid)).thenThrow(new CustomGigyaErrorException(("")));
        doNothing().when(notificationService).sendAccountMergedNotification(any());

        // when
        accountRequestService.onAccountMerged(uid);

        // then
        verify(notificationService, times(0)).sendAccountMergedNotification(mergedAccountNotification);
    }

    @Test
    public void givenOnAccountMergedIsCalled_WhenAccountIsNotFederated_ThenAccountMergedNotificationShouldNotBeSent() throws CustomGigyaErrorException {
        // when
        String uid = AccountUtils.uid;
        AccountInfo accountMock = AccountUtils.getSiteAccount();
        MergedAccountNotification mergedAccountNotification = MergedAccountNotification.build(accountMock);
        when(cdcResponseHandler.getAccountInfo(uid)).thenReturn(accountMock);
        doNothing().when(notificationService).sendAccountMergedNotification(any());

        try (MockedStatic<MergedAccountNotification> mergedAccountNotificationStatic = Mockito.mockStatic(MergedAccountNotification.class)) {
            // when
            mergedAccountNotificationStatic.when(() -> MergedAccountNotification.build(any())).thenReturn(mergedAccountNotification);
            accountRequestService.onAccountMerged(uid);

            // then
            verify(notificationService, never()).sendAccountMergedNotification(any());
        }
    }

    @Test
    public void onAccountUpdated_GivenAccountIsFederated_ThenTheAccountUpdatedNotificationShouldBeSent() throws IOException, CustomGigyaErrorException {
        // given
        String uid = UUID.randomUUID().toString();
        when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(AccountUtils.getFederatedAccount());
        doNothing().when(notificationService).sendPrivateAccountUpdatedNotification(any());

        // when
        accountRequestService.onAccountUpdated(uid);

        // then
        verify(notificationService).sendPrivateAccountUpdatedNotification(any());
    }

    @Test
    public void onAccountUpdated_GivenAccountIsFederated_ThenTheAccountUpdatedNotificationShouldNotBeSent() throws IOException, CustomGigyaErrorException {
        // given
        String uid = UUID.randomUUID().toString();
        when(cdcResponseHandler.getAccountInfo(anyString())).thenReturn(AccountUtils.getSiteAccount());
        doNothing().when(notificationService).sendPrivateAccountUpdatedNotification(any());

        // when
        accountRequestService.onAccountUpdated(uid);

        // then
        verify(notificationService, never()).sendPrivateAccountUpdatedNotification(any());
    }

    @Test
    public void onAccountUpdated_WhenCustomGigyaErrorExceptionIsThrown_ThenAccountUpdatedNotificationShouldNotBeSent() throws CustomGigyaErrorException {
        // given
        String uid = AccountUtils.uid;
        when(cdcResponseHandler.getAccountInfo(uid)).thenThrow(new CustomGigyaErrorException(("")));
        doNothing().when(notificationService).sendPrivateAccountUpdatedNotification(any());

        // when
        accountRequestService.onAccountUpdated(uid);

        // then
        verify(notificationService, times(0)).sendPrivateAccountUpdatedNotification(any());
    }
}
