package com.thermofisher.cdcam.services;


import com.gigya.socialize.GSKeyNotFoundException;
import com.thermofisher.cdcam.aws.SNSHandler;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccountV2;
import com.thermofisher.cdcam.model.cdc.CDCResponse;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CDCValidationError;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.OpenIdProvider;
import com.thermofisher.cdcam.model.cdc.OpenIdRelyingParty;
import com.thermofisher.cdcam.model.dto.ConsentDTO;
import com.thermofisher.cdcam.model.notifications.MergedAccountNotification;
import com.thermofisher.cdcam.utils.AccountUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountsServiceTests {
    private final List<String> uids = new ArrayList<>();

    @InjectMocks
    AccountsService accountsService;

    @Mock
    GigyaService gigyaService;

    @Mock
    NotificationService notificationService;

    @Mock
    SNSHandler snsHandler;

    @Mock
    SecretsService secretsService;

    @Captor
    ArgumentCaptor<CDCAccount> cdcAccountCaptor;

    @Captor
    ArgumentCaptor<Map<String, String>> mapCaptor;

    private AccountInfo federationAccount;

    @BeforeEach
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
    public void onAccountRegistered_GivenUIDisValid_ThenGetAccountInfo() throws IOException, CustomGigyaErrorException {
        // given
        String uid = UUID.randomUUID().toString();
        when(gigyaService.getAccountInfo(anyString())).thenReturn(federationAccount);

        // when
        accountsService.onAccountRegistered(uid);

        // then
        verify(gigyaService).getAccountInfo(any());
    }

    @Test
    public void onAccountRegistered_ThenSaveAWSQuickSightRole() throws IOException, CustomGigyaErrorException, JSONException {
        // given
        String uid = UUID.randomUUID().toString();
        String mockQuickSightRole = RandomStringUtils.random(10);
        when(gigyaService.getAccountInfo(anyString())).thenReturn(federationAccount);
        when(secretsService.get(anyString())).thenReturn(mockQuickSightRole);
        doNothing().when(gigyaService).setAccountInfo(any(CDCAccount.class));

        // when
        accountsService.onAccountRegistered(uid);

        // then
        verify(gigyaService).setAccountInfo(cdcAccountCaptor.capture());
        CDCAccount capturedCdcAccount = cdcAccountCaptor.getValue();
        assertEquals(mockQuickSightRole, capturedCdcAccount.getData().getAwsQuickSightRole());
    }

    @Test
    public void onAccountRegistered_ThenSaveOpenIdProviderDescription() throws IOException, CustomGigyaErrorException, JSONException, GSKeyNotFoundException {
        // given
        String uid = UUID.randomUUID().toString();

        String providerClientId = RandomStringUtils.random(10);
        String providerDescriptionMock = RandomStringUtils.random(10);
        OpenIdRelyingParty rpMock = OpenIdRelyingParty.builder().clientId(providerClientId).description(providerDescriptionMock).build();
        when(gigyaService.getRP(anyString())).thenReturn(rpMock);

        federationAccount.setOpenIdProviderId(providerClientId);
        when(gigyaService.getAccountInfo(anyString())).thenReturn(federationAccount);
        doNothing().when(gigyaService).setAccountInfo(any(CDCAccount.class));

        // when
        accountsService.onAccountRegistered(uid);

        // then
        verify(gigyaService).setAccountInfo(cdcAccountCaptor.capture());
        CDCAccount capturedCdcAccount = cdcAccountCaptor.getValue();
        String providerDescriptionResult = capturedCdcAccount.getData()
                .getRegistration()
                .getOpenIdProvider()
                .getProviderName();
        assertEquals(providerDescriptionMock, providerDescriptionResult);
    }

    @Test
    public void onAccountRegistered_GivenAccountDoesntHaveProvider_ThenShouldNotFetchRPData_AndSavedProviderShouldBeNull() throws IOException, CustomGigyaErrorException, JSONException, GSKeyNotFoundException {
        // given
        String uid = UUID.randomUUID().toString();
//        when(gigyaService.getRP(anyString())).thenCallRealMethod();

        when(gigyaService.getAccountInfo(anyString())).thenReturn(federationAccount);
        doNothing().when(gigyaService).setAccountInfo(any(CDCAccount.class));

        // when
        accountsService.onAccountRegistered(uid);

        // then
        verify(gigyaService, never()).getRP(anyString());
        verify(gigyaService).setAccountInfo(cdcAccountCaptor.capture());
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
        when(gigyaService.getAccountInfo(anyString())).thenReturn(null);

        // when
        accountsService.onAccountRegistered(uid);
    }

    @Test
    public void onAccountRegistered_GivenNewAccountRegistered_ThenSendAccountRegistrationNotification() throws IOException, CustomGigyaErrorException {
        // given
        ReflectionTestUtils.setField(accountsService, "cipdc", "us");
        ReflectionTestUtils.setField(accountsService, "isRegistrationNotificationEnabled", true);
        String uid = UUID.randomUUID().toString();
        when(gigyaService.getAccountInfo(anyString())).thenReturn(AccountUtils.getFederatedAccount());
        doNothing().when(notificationService).sendAccountRegisteredNotification(any(), anyString());

        // when
        accountsService.onAccountRegistered(uid);

        // then
        verify(notificationService).sendAccountRegisteredNotification(any(), anyString());
    }

    @Test
    public void processRegistrationRequest_givenANullAccount_ThrowNullPointerException() throws NoSuchAlgorithmException, JSONException, IOException, CustomGigyaErrorException {
        Assertions.assertThrows(NullPointerException.class, () -> {
            accountsService.createAccount(null);
        });
    }

    @Test
    public void processRegistrationRequest_givenAValidAccount_returnCDCResponseData() throws IOException, NoSuchAlgorithmException, JSONException, CustomGigyaErrorException {
        // given
        ReflectionTestUtils.setField(gigyaService, "isNewMarketingConsentEnabled", false);
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID("9f6f2133e57144d787574d49c0b9908e");
        cdcResponseData.setStatusCode(200);
        when(gigyaService.register(any(CDCNewAccount.class))).thenReturn(cdcResponseData);

        // when
        accountsService.createAccount(accountInfo);

        // then
        verify(gigyaService).register(any(CDCNewAccount.class));
    }

    @Test
    public void processRegistrationRequest_givenAValidAccount_returnCDCResponseData_V2() throws IOException, NoSuchAlgorithmException, JSONException, CustomGigyaErrorException {
        // given
        ReflectionTestUtils.setField(accountsService, "isNewMarketingConsentEnabled", true);
        AccountInfo accountInfo = AccountUtils.getSiteAccount();
        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID("9f6f2133e57144d787574d49c0b9908e");
        cdcResponseData.setStatusCode(200);
        when(gigyaService.register(any(CDCNewAccountV2.class))).thenReturn(cdcResponseData);

        // when
        accountsService.createAccount(accountInfo);

        // then
        verify(gigyaService).register(any(CDCNewAccountV2.class));
    }

    @Test
    public void processRegistrationRequest_GivenCDCReturnsAnErrorResponse_ThenThrowCustomGigyaErrorException() throws IOException, NoSuchAlgorithmException, JSONException, CustomGigyaErrorException {
        // given
        ReflectionTestUtils.setField(accountsService, "isNewMarketingConsentEnabled", false);
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
        when(gigyaService.register(any(CDCNewAccount.class))).thenReturn(cdcResponseData);

        Assertions.assertThrows(CustomGigyaErrorException.class, () -> {
            accountsService.createAccount(accountInfo);
        });
    }

    @Test
    public void processRegistrationRequest_GivenCDCReturnsAnErrorResponse_ThenThrowCustomGigyaErrorException_V2() throws IOException, NoSuchAlgorithmException, JSONException, CustomGigyaErrorException {
        // given
        ReflectionTestUtils.setField(accountsService, "isNewMarketingConsentEnabled", true);
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
        when(gigyaService.register(any(CDCNewAccountV2.class))).thenReturn(cdcResponseData);

        Assertions.assertThrows(CustomGigyaErrorException.class, () -> {
            accountsService.createAccount(accountInfo);
        });
    }

    @Test
    public void givenOnAccountMergedIsCalled_WhenUidParameterIsNull_ThenNullPointerExceptionShouldBeThrown() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            accountsService.onAccountMerged(null);
        });
    }

    @Test
    public void givenOnAccountMergedIsCalled_WhenAccountIsFederated_ThenAccountMergedNotificationShouldBeSent() throws CustomGigyaErrorException {
        // when
        String uid = AccountUtils.uid;
        AccountInfo accountMock = AccountUtils.getFederatedAccount();
        MergedAccountNotification mergedAccountNotification = MergedAccountNotification.build(accountMock);
        when(gigyaService.getAccountInfo(uid)).thenReturn(accountMock);
        doNothing().when(notificationService).sendAccountMergedNotification(any());

        try (MockedStatic<MergedAccountNotification> mergedAccountNotificationStatic = Mockito.mockStatic(MergedAccountNotification.class)) {
            // when
            mergedAccountNotificationStatic.when(() -> MergedAccountNotification.build(any())).thenReturn(mergedAccountNotification);
            accountsService.onAccountMerged(uid);

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
        when(gigyaService.getAccountInfo(uid)).thenThrow(new CustomGigyaErrorException(("")));
//        doNothing().when(notificationService).sendAccountMergedNotification(any());

        // when
        accountsService.onAccountMerged(uid);

        // then
        verify(notificationService, times(0)).sendAccountMergedNotification(mergedAccountNotification);
    }

    @Test
    public void givenOnAccountMergedIsCalled_WhenAccountIsNotFederated_ThenAccountMergedNotificationShouldNotBeSent() throws CustomGigyaErrorException {
        // when
        String uid = AccountUtils.uid;
        AccountInfo accountMock = AccountUtils.getSiteAccount();
        MergedAccountNotification mergedAccountNotification = MergedAccountNotification.build(accountMock);
        when(gigyaService.getAccountInfo(uid)).thenReturn(accountMock);
//        doNothing().when(notificationService).sendAccountMergedNotification(any());

        try (MockedStatic<MergedAccountNotification> mergedAccountNotificationStatic = Mockito.mockStatic(MergedAccountNotification.class)) {
            // when
            mergedAccountNotificationStatic.when(() -> MergedAccountNotification.build(any())).thenReturn(mergedAccountNotification);
            accountsService.onAccountMerged(uid);

            // then
            verify(notificationService, never()).sendAccountMergedNotification(any());
        }
    }

    @Test
    public void onAccountUpdated_GivenAccountIsFederated_ThenTheAccountUpdatedNotificationShouldBeSent() throws IOException, CustomGigyaErrorException {
        // given
        String uid = UUID.randomUUID().toString();
        when(gigyaService.getAccountInfo(anyString())).thenReturn(AccountUtils.getFederatedAccount());
        doNothing().when(notificationService).sendPrivateAccountUpdatedNotification(any());

        // when
        accountsService.onAccountUpdated(uid);

        // then
        verify(notificationService).sendPrivateAccountUpdatedNotification(any());
    }

    @Test
    public void onAccountUpdated_GivenAccountIsFederated_ThenTheAccountUpdatedNotificationShouldNotBeSent() throws IOException, CustomGigyaErrorException {
        // given
        String uid = UUID.randomUUID().toString();
        when(gigyaService.getAccountInfo(anyString())).thenReturn(AccountUtils.getSiteAccount());
//        doNothing().when(notificationService).sendPrivateAccountUpdatedNotification(any());

        // when
        accountsService.onAccountUpdated(uid);

        // then
        verify(notificationService, never()).sendPrivateAccountUpdatedNotification(any());
    }

    @Test
    public void onAccountUpdated_WhenCustomGigyaErrorExceptionIsThrown_ThenAccountUpdatedNotificationShouldNotBeSent() throws CustomGigyaErrorException {
        // given
        String uid = AccountUtils.uid;
        when(gigyaService.getAccountInfo(uid)).thenThrow(new CustomGigyaErrorException(("")));
//        doNothing().when(notificationService).sendPrivateAccountUpdatedNotification(any());

        // when
        accountsService.onAccountUpdated(uid);

        // then
        verify(notificationService, times(0)).sendPrivateAccountUpdatedNotification(any());
    }

    @Test
    public void verify_ShouldVerifyAnAccount() throws CustomGigyaErrorException, JSONException {
        // given
        String regToken = "regTokenTest";
        AccountInfo account = AccountUtils.getSiteAccount();
        when(gigyaService.setAccountInfo(anyMap())).thenReturn(AccountUtils.getCdcResponse());
        when(gigyaService.finalizeRegistration(anyString())).thenReturn(AccountUtils.getCdcResponse());

        // when
        accountsService.verify(account, regToken);

        // then
        verify(gigyaService).setAccountInfo(mapCaptor.capture());
        Map<String, String> params = mapCaptor.getValue();
        assertEquals(params.get("UID"), account.getUid());
        assertTrue(Boolean.parseBoolean(params.get("isVerified")));
    }

    @Test
    public void updateConsent_givenMarketingConsentTrue_shouldCallSetAccountInfoWithAdditionalFields() throws CustomGigyaErrorException, JSONException {
        // Given.
        ConsentDTO request = ConsentDTO.builder()
                .uid("abc123")
                .marketingConsent(true)
                .city("Carlsbad")
                .company("Thermo Fisher Scientific")
                .build();

        when(gigyaService.setAccountInfo(any(Map.class))).thenReturn(Mockito.mock(CDCResponse.class));

        ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

        // When.
        accountsService.updateConsent(request);

        // Then.
        verify(gigyaService).setAccountInfo(paramsCaptor.capture());

        Map<String, String> params = paramsCaptor.getValue();
        assertEquals(params.get("UID"), request.getUid());

        JSONObject preferences = new JSONObject(params.get("preferences"));
        assertEquals(preferences.getJSONObject("marketing").getJSONObject("consent").getBoolean("isConsentGranted"), request.getMarketingConsent());

        JSONObject profile = new JSONObject(params.get("profile"));
        assertEquals(profile.getJSONObject("work").getString("company"), request.getCompany());
        assertEquals(profile.getString("city"), request.getCity());
    }

    @Test
    public void updateConsent_givenMarketingConsentFalse_shouldCallSetAccountInfoWithoutAdditionalFields() throws CustomGigyaErrorException, JSONException {
        // Given.
        ConsentDTO request = ConsentDTO.builder()
                .uid("abc123")
                .marketingConsent(false)
                .city("Carlsbad")
                .company("Thermo Fisher Scientific")
                .build();

        when(gigyaService.setAccountInfo(any(Map.class))).thenReturn(Mockito.mock(CDCResponse.class));

        ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

        // When.
        accountsService.updateConsent(request);

        // Then.
        verify(gigyaService).setAccountInfo(paramsCaptor.capture());

        Map<String, String> params = paramsCaptor.getValue();
        assertEquals(params.get("UID"), request.getUid());

        JSONObject preferences = new JSONObject(params.get("preferences"));
        assertEquals(preferences.getJSONObject("marketing").getJSONObject("consent").getBoolean("isConsentGranted"), request.getMarketingConsent());

        assertFalse(params.containsKey("profile"));
    }

    @Test
    public void notifyUpdatedConsent_shouldFetchAccountInfo_andSendPublicUpdateNotification() throws CustomGigyaErrorException {
        // Given.
        String uid = "abc123";

        AccountInfo mockAccountInfo = AccountInfo.builder().build();

        when(gigyaService.getAccountInfo(uid)).thenReturn(mockAccountInfo);

        doNothing().when(notificationService).sendPublicAccountUpdatedNotification(any());

        // When.
        accountsService.notifyUpdatedConsent(uid);

        // Then.
        verify(gigyaService).getAccountInfo(uid);
        verify(notificationService).sendPublicAccountUpdatedNotification(any());
    }
}
