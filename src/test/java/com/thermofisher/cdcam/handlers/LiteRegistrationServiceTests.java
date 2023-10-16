package com.thermofisher.cdcam.handlers;


import com.gigya.socialize.GSKeyNotFoundException;
import com.thermofisher.cdcam.enums.ResponseCode;
import com.thermofisher.cdcam.enums.cdc.DataCenter;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EECUserV1;
import com.thermofisher.cdcam.model.EECUserV2;
import com.thermofisher.cdcam.model.EECUserV3;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CDCSearchResponse;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.Profile;
import com.thermofisher.cdcam.model.cdc.SearchResponse;
import com.thermofisher.cdcam.model.dto.LiteAccountDTO;
import com.thermofisher.cdcam.services.GigyaService;
import com.thermofisher.cdcam.utils.cdc.LiteRegistrationService;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LiteRegistrationServiceTests {

    private static final String ERROR_MSG = "Something went wrong, please contact the system administrator.";
    private static final String MOCKED_UID = "59b44e6023214be5846c9cbd4cedfe93";
    private static final String MOCKED_EMAIL_1 = "account-test1@test.com";
    private static final String MOCKED_REG_REDIRECT_URI = "https://test.com/registration?uid={1}";
    private static final String RESPONSE_MESSAGE_OK = "OK";
    private static final int RESPONSE_CODE_SUCCESS = 200;
    private static final int RESPONSE_CODE_ACCOUNT_AREADY_EXISTS = 4001;
    private static final int RESPONSE_CODE_BAD_REQUEST = 400;
    private static final int RESPONSE_CODE_GENERIC_ERROR = 500;
    private static final int MOCKED_GIGYA_ERROR_CODE = 400004;
    private static final String MOCKED_GIGYA_ERROR_MSG = "MOCKED-GIGYA-ERROR-MSG";

    @InjectMocks
    LiteRegistrationService liteRegistrationService;

    @Mock
    GigyaService gigyaService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private void setProperties() {
        ReflectionTestUtils.setField(liteRegistrationService, "mainDataCenterName", "us");
        ReflectionTestUtils.setField(liteRegistrationService, "registrationRedirectionUri", MOCKED_REG_REDIRECT_URI);
        ReflectionTestUtils.setField(liteRegistrationService, "requestLimitV3", 3);
    }

    private CDCAccount buildAccount(String uid, boolean isActive, boolean isRegistered, Profile profile) {
        return CDCAccount.builder()
        .UID(uid)
        .isActive(isActive)
        .isRegistered(isRegistered)
        .profile(profile)
        .build();
    }

    @Test
    public void createLiteAccountsV2_GivenTheEmailListContainsAnyNullEmail_ThrowIllegalArgumentException() throws IOException {
        // given
        ArrayList<String> emails = new ArrayList<String>();
        emails.add(null);
        EmailList emailList = EmailList.builder().emails(emails).build();

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            liteRegistrationService.registerEmailAccounts(emailList);
        });
    }

    @Test
    public void createLiteAccountsV2_GivenTheEmailListContainsAnyEmptyEmail_ThrowIllegalArgumentException() throws IOException {
        // given
        ArrayList<String> emails = new ArrayList<String>();
        emails.add("");
        EmailList emailList = EmailList.builder().emails(emails).build();

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            liteRegistrationService.registerEmailAccounts(emailList);
        });
    }

    @Test
    public void createLiteAccountsV2_GivenEmailValidationIsEnabled_AndTheEmailListContainsAnInvalidEmail_ThenReturnEECUserWith400Error() throws IOException, CustomGigyaErrorException {
        // given
        final boolean isActive = false;
        final boolean isRegistered = false;
        final String errorMessage = "Email is invalid.";
        final int errorCode = 400;

        ReflectionTestUtils.setField(liteRegistrationService, "isEmailValidationEnabled", true);
        CDCAccount account = buildAccount(MOCKED_UID, isActive, isRegistered, null);
        List<CDCAccount> accounts = new ArrayList<>();
        accounts.add(account);
        CDCSearchResponse cdcSearchResponse = new CDCSearchResponse();
        cdcSearchResponse.setResults(accounts);
        SearchResponse searchResponse = SearchResponse.builder().cdcSearchResponse(cdcSearchResponse).dataCenter(DataCenter.US).build();
//        when(gigyaService.search(anyString(), any(), anyString())).thenReturn(cdcSearchResponse);
        when(gigyaService.searchInBothDC(anyString())).thenReturn(searchResponse);

        ArrayList<String> emails = new ArrayList<>();
        emails.add("inavalid@.com");
        emails.add("valid@test.com");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUserV2> output = liteRegistrationService.registerEmailAccounts(emailList);

        EECUser user = output.get(0);
        Assertions.assertNull(user.getUid());
        Assertions.assertEquals(errorMessage, user.getResponseMessage());
        Assertions.assertEquals(errorCode, user.getResponseCode());
    }

    @Test
    public void createLiteAccountsV2_givenEmailListEmpty_returnEmptyEECUserList() throws IOException {
        // given
        EmailList emailList = EmailList.builder().emails(new ArrayList<>()).build();

        // when
        List<EECUserV2> output = liteRegistrationService.registerEmailAccounts(emailList);

        // then
        Assertions.assertTrue(output.isEmpty());
    }

    @Test
    public void createLiteAccountsV2_givenAnEmailListIsPassed_ThenTheResultShouldContainTheSameAmountOfItems() throws IOException, CustomGigyaErrorException {
        // given
        final Boolean isActive = false;
        final Boolean isRegistered = false;
        CDCAccount account = buildAccount(MOCKED_UID, isActive, isRegistered, null);
        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        accounts.add(account);
        CDCSearchResponse searchResponse = new CDCSearchResponse();
        searchResponse.setResults(accounts);
//        when(gigyaService.search(anyString(), any(), anyString())).thenReturn(searchResponse);

        List<String> emails = new ArrayList<String>();
        emails.add("test1@mail.com");
        emails.add("test2@mail.com");
        emails.add("test3@mail.com");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUserV2> result = liteRegistrationService.registerEmailAccounts(emailList);

        // then
        Assertions.assertEquals(result.size(), emails.size());
    }

    @Test
    public void createLiteAccountsV2_givenAnEmailAlreadyExists_ThenResultShouldContainTheFoundAccountData() throws IOException, CustomGigyaErrorException {
        // given
        final Boolean isActive = true;
        final Boolean isRegistered = true;
        final Boolean isAvailable = false;
        final String username = "test@mail.com";
        Profile profile = Profile.builder().username(username).build();
        CDCAccount account = buildAccount(MOCKED_UID, isActive, isRegistered, profile);
        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        accounts.add(account);
        CDCSearchResponse cdcSearchResponse = new CDCSearchResponse();
        cdcSearchResponse.setResults(accounts);
        SearchResponse searchResponse = SearchResponse.builder().cdcSearchResponse(cdcSearchResponse).dataCenter(DataCenter.US).build();
//        when(gigyaService.search(anyString(), any(), any())).thenReturn(cdcSearchResponse);
        when(gigyaService.searchInBothDC(anyString())).thenReturn(searchResponse);

        List<String> emails = new ArrayList<String>();
        emails.add("test1@mail.com");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUserV2> result = liteRegistrationService.registerEmailAccounts(emailList);
        
        // then
        EECUserV2 userV2 = (EECUserV2) result.get(0);
        Assertions.assertEquals(MOCKED_UID, userV2.getUid());
        Assertions.assertEquals(username, userV2.getUsername());
        Assertions.assertEquals(isRegistered, userV2.getIsRegistered());
        Assertions.assertEquals(isActive, userV2.getIsActive());
        Assertions.assertEquals(isAvailable, userV2.getIsAvailable());
    }

    @Test
    public void createLiteAccountsV2_givenEmailIsNotFound_ThenItShouldBeLiteRegisteredAndOnlyContainItsUID_IsRegisteredAsFalse_AndIsActiveAsFalse() throws IOException, CustomGigyaErrorException, GSKeyNotFoundException {
        // given
        final Boolean isAvailable = true;

        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        CDCSearchResponse cdcSearchResponse = new CDCSearchResponse();
        cdcSearchResponse.setResults(accounts);
        SearchResponse searchResponse = SearchResponse.builder().cdcSearchResponse(cdcSearchResponse).dataCenter(DataCenter.US).build();
//        when(gigyaService.search(anyString(), any(), any())).thenReturn(cdcSearchResponse);
        when(gigyaService.searchInBothDC(anyString())).thenReturn(searchResponse);

        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID(MOCKED_UID);
        when(gigyaService.registerLiteAccount(anyString())).thenReturn(cdcResponseData);

        List<String> emails = new ArrayList<String>();
        emails.add("test1@mail.com");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUserV2> output = liteRegistrationService.registerEmailAccounts(emailList);
        
        // then
        EECUserV2 userV2 = (EECUserV2) output.get(0);
        Assertions.assertEquals(MOCKED_UID, userV2.getUid());
        Assertions.assertNull(userV2.getUsername());
        Assertions.assertFalse(userV2.getIsRegistered());
        Assertions.assertFalse(userV2.getIsActive());
        Assertions.assertEquals(isAvailable, userV2.getIsAvailable());
        verify(gigyaService).registerLiteAccount(anyString());
    }

    @Test
    public void createLiteAccountsV2_givenSearchThrowsCustomGigyaErrorException_returnEECUserWith500Error() throws IOException, CustomGigyaErrorException {
        // given
        final String errorMessage = "Error";
        final int errorCode = 400;

        when(gigyaService.searchInBothDC(anyString())).thenThrow(new CustomGigyaErrorException(errorMessage, errorCode));

        List<String> emails = new ArrayList<String>();
        emails.add("test1@mail.com");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUserV2> output = liteRegistrationService.registerEmailAccounts(emailList);

        // then
        EECUser user = output.get(0);
        Assertions.assertEquals(errorMessage, user.getResponseMessage());
        Assertions.assertEquals(errorCode, user.getResponseCode());
    }

    @Test
    public void createLiteAccountsV2_givenSearchThrowsAnyOtherException_returnEECUserWith500Error() throws IOException, CustomGigyaErrorException {
        // given
//        when(gigyaService.search(anyString(), any(), anyString())).thenThrow(new IOException());
        
        List<String> emails = new ArrayList<String>();
        emails.add("test1@mail.com");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUserV2> output = liteRegistrationService.registerEmailAccounts(emailList);

        // then
        EECUser user = output.get(0);
        Assertions.assertNull(user.getUid());
        Assertions.assertEquals(ERROR_MSG, user.getResponseMessage());
        Assertions.assertEquals(500, user.getResponseCode());
    }

    @Test
    public void createLiteAccountsV1_GivenTheEmailListContainsAnyNullEmail_ThrowIllegalArgumentException() throws IOException {
        // given
        ArrayList<String> emails = new ArrayList<String>();
        emails.add(null);
        EmailList emailList = EmailList.builder().emails(emails).build();

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            liteRegistrationService.createLiteAccountsV1(emailList);
        });
    }

    @Test
    public void createLiteAccountsV1_GivenTheEmailListContainsAnyEmptyEmail_ThrowIllegalArgumentException() throws IOException {
        // given
        ArrayList<String> emails = new ArrayList<String>();
        emails.add("");
        EmailList emailList = EmailList.builder().emails(emails).build();

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            liteRegistrationService.createLiteAccountsV1(emailList);
        });
    }

    @Test
    public void createLiteAccountsV1_givenEmailListEmpty_returnEmptyEECUserList() throws IOException {
        // given
        EmailList emailList = EmailList.builder().emails(new ArrayList<>()).build();

        // when
        List<EECUser> output = liteRegistrationService.createLiteAccountsV1(emailList);

        // then
        Assertions.assertTrue(output.isEmpty());
    }

    @Test
    public void createLiteAccountsV1_givenAnEmailListIsPassed_ThenTheResultShouldContainTheSameAmountOfItems() throws IOException, CustomGigyaErrorException {
        // given
        final Boolean isActive = false;
        final Boolean isRegistered = false;
        CDCAccount account = buildAccount(MOCKED_UID, isActive, isRegistered, null);

        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        accounts.add(account);
        CDCSearchResponse searchResponse = new CDCSearchResponse();
        searchResponse.setResults(accounts);
//        when(gigyaService.search(anyString(), any(), anyString())).thenReturn(searchResponse);

        List<String> emails = new ArrayList<String>();
        emails.add("test1@mail.com");
        emails.add("test2@mail.com");
        emails.add("test3@mail.com");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> result = liteRegistrationService.createLiteAccountsV1(emailList);

        // then
        Assertions.assertEquals(result.size(), emails.size());
    }

    @Test
    public void createLiteAccountsV1_givenAnEmailAlreadyExists_ThenResultShouldContainTheFoundAccountData() throws IOException, CustomGigyaErrorException {
        // given
        setProperties();
        final Boolean isActive = true;
        final Boolean isRegistered = true;
        final Boolean isAvailable = false;
        final String username = "test@mail.com";
        Profile profile = Profile.builder().username(username).build();
        CDCAccount account = buildAccount(MOCKED_UID, isActive, isRegistered, profile);

        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        accounts.add(account);
        CDCSearchResponse cdcSearchResponse = new CDCSearchResponse();
        cdcSearchResponse.setResults(accounts);
        SearchResponse searchResponse = SearchResponse.builder().cdcSearchResponse(cdcSearchResponse).dataCenter(DataCenter.US).build();
//        when(gigyaService.search(anyString(), any(), any())).thenReturn(cdcSearchResponse);
        when(gigyaService.searchInBothDC(anyString())).thenReturn(searchResponse);

        List<String> emails = new ArrayList<String>();
        emails.add("test1@mail.com");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> result = liteRegistrationService.createLiteAccountsV1(emailList);
        
        // then
        EECUserV1 userV1 = (EECUserV1) result.get(0);
        Assertions.assertEquals(MOCKED_UID, userV1.getUid());
        Assertions.assertEquals(username, userV1.getUsername());
        Assertions.assertEquals(isRegistered, userV1.getRegistered());
        Assertions.assertEquals(isAvailable, userV1.getIsAvailable());
    }

    @Test
    public void createLiteAccountsV1_givenEmailIsNotFound_ThenItShouldBeLiteRegisteredAndOnlyContainItsUID_RegisteredAsFalse() throws IOException, CustomGigyaErrorException, GSKeyNotFoundException {
        // given
        final Boolean isAvailable = true;
        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        CDCSearchResponse cdcSearchResponse = new CDCSearchResponse();
        cdcSearchResponse.setResults(accounts);
        SearchResponse searchResponse = SearchResponse.builder().cdcSearchResponse(cdcSearchResponse).dataCenter(DataCenter.US).build();
  //      when(gigyaService.search(anyString(), any(), any())).thenReturn(cdcSearchResponse);
        when(gigyaService.searchInBothDC(anyString())).thenReturn(searchResponse);

        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID(MOCKED_UID);
        when(gigyaService.registerLiteAccount(anyString())).thenReturn(cdcResponseData);

        List<String> emails = new ArrayList<String>();
        emails.add("test1@mail.com");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> output = liteRegistrationService.createLiteAccountsV1(emailList);
        
        // then
        EECUserV1 userV1 = (EECUserV1) output.get(0);
        Assertions.assertEquals(MOCKED_UID, userV1.getUid());
        Assertions.assertNull(userV1.getUsername());
        Assertions.assertFalse(userV1.getRegistered());
        Assertions.assertEquals(isAvailable, userV1.getIsAvailable());
        verify(gigyaService).registerLiteAccount(anyString());
    }

    @Test
    public void createLiteAccountsV1_givenSearchThrowsCustomGigyaErrorException_returnEECUserWith500Error() throws IOException, CustomGigyaErrorException {
        // given
        final String errorMessage = "Error";
        final int errorCode = 400;

        when(gigyaService.searchInBothDC(anyString())).thenThrow(new CustomGigyaErrorException(errorMessage, errorCode));

        List<String> emails = new ArrayList<String>();
        emails.add("test1@mail.com");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> output = liteRegistrationService.createLiteAccountsV1(emailList);

        // then
        EECUser user = output.get(0);
        Assertions.assertEquals(errorMessage, user.getResponseMessage());
        Assertions.assertEquals(errorCode, user.getResponseCode());
    }

    @Test
    public void createLiteAccountsV1_givenSearchThrowsAnyOtherException_returnEECUserWith500Error() throws IOException, CustomGigyaErrorException {
        // given
//        when(gigyaService.search(anyString(), any(), anyString())).thenThrow(new IOException());
        
        List<String> emails = new ArrayList<String>();
        emails.add("test1@mail.com");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> output = liteRegistrationService.createLiteAccountsV1(emailList);

        // then
        EECUser user = output.get(0);
        Assertions.assertNull(user.getUid());
        Assertions.assertEquals(ERROR_MSG, user.getResponseMessage());
        Assertions.assertEquals(500, user.getResponseCode());
    }

    @Test
    public void createLiteAccountsV1_givenAnEmailAlreadyExists_ThenResponseCodeShouldBeSuccess() throws IOException, CustomGigyaErrorException {
        // given
        setProperties();
        final Boolean isActive = true;
        final Boolean isRegistered = true;
        final String username = "test@mail.com";
        Profile profile = Profile.builder().username(username).build();
        CDCAccount account = buildAccount(MOCKED_UID, isActive, isRegistered, profile);

        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        accounts.add(account);
        CDCSearchResponse cdcSearchResponse = new CDCSearchResponse();
        cdcSearchResponse.setResults(accounts);
        SearchResponse searchResponse = SearchResponse.builder().cdcSearchResponse(cdcSearchResponse).dataCenter(DataCenter.US).build();
//        when(gigyaService.search(anyString(), any(), any())).thenReturn(cdcSearchResponse);
        when(gigyaService.searchInBothDC(anyString())).thenReturn(searchResponse);

        List<String> emails = new ArrayList<String>();
        emails.add("test1@mail.com");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> result = liteRegistrationService.createLiteAccountsV1(emailList);
        
        // then
        EECUserV1 userV1 = (EECUserV1) result.get(0);
        Assertions.assertEquals(ResponseCode.SUCCESS.getValue(), userV1.getResponseCode());
    }

    @Test
    public void registerLiteAccounts_givenValidAccount_thenReturnSuccess() throws CustomGigyaErrorException, IOException, GSKeyNotFoundException, JSONException {
        // given
        setProperties();
        when( gigyaService.searchInBothDC(MOCKED_EMAIL_1))
            .thenReturn(
                SearchResponse
                    .builder()
                    .cdcSearchResponse(
                        CDCSearchResponse
                            .builder()
                            .results(Collections.emptyList())
                            .build()
                    )
                    .build()
            );
        when(gigyaService.registerLiteAccount(any(LiteAccountDTO.class)))
            .thenReturn(
                CDCResponseData
                    .builder()
                    .UID(MOCKED_UID)
                    .build()
            );

        LiteAccountDTO account = LiteAccountDTO.builder()
                .email(MOCKED_EMAIL_1)
                .build();

        List<LiteAccountDTO> request = Collections.singletonList(account);

        // when
        List<EECUserV3> result = liteRegistrationService.registerLiteAccounts(request);

        // then
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(MOCKED_EMAIL_1, result.get(0).getEmail());
        Assertions.assertEquals(MOCKED_UID, result.get(0).getUid());
        Assertions.assertEquals(MOCKED_REG_REDIRECT_URI, result.get(0).getPasswordSetupLink());
        Assertions.assertEquals(RESPONSE_CODE_SUCCESS, result.get(0).getResponseCode());
        Assertions.assertEquals(RESPONSE_MESSAGE_OK, result.get(0).getResponseMessage());

    }

    @Test
    public void registerLiteAccounts_givenValidAccountWithoutClientId_thenReturnSuccessWithDefaultClientId() throws Exception {
        // given
        setProperties();
        when( gigyaService.searchInBothDC(MOCKED_EMAIL_1))
            .thenReturn(
                SearchResponse
                    .builder()
                    .cdcSearchResponse(
                        CDCSearchResponse
                            .builder()
                            .results(Collections.emptyList())
                            .build()
                    )
                    .build()
            );
        when(gigyaService.registerLiteAccount(any(LiteAccountDTO.class)))
            .thenReturn(
                CDCResponseData
                    .builder()
                    .UID(MOCKED_UID)
                    .build()
            );

        LiteAccountDTO account = LiteAccountDTO.builder()
                .email(MOCKED_EMAIL_1)
                .build();

        List<LiteAccountDTO> request = Collections.singletonList(account);

        // when
        List<EECUserV3> result = liteRegistrationService.registerLiteAccounts(request);

        // then
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(MOCKED_EMAIL_1, result.get(0).getEmail());
        Assertions.assertEquals(MOCKED_UID, result.get(0).getUid());
        Assertions.assertEquals(MOCKED_REG_REDIRECT_URI, result.get(0).getPasswordSetupLink());
        Assertions.assertEquals(RESPONSE_CODE_SUCCESS, result.get(0).getResponseCode());
        Assertions.assertEquals(RESPONSE_MESSAGE_OK, result.get(0).getResponseMessage());

    }

    @Test
    public void registerLiteAccounts_givenEmptyEmail_thenReturnBadRequestCode() throws Exception {
        // given
        setProperties();
        List<LiteAccountDTO> request = Collections.singletonList(LiteAccountDTO.builder().email("").build());

        // when
        List<EECUserV3> result = liteRegistrationService.registerLiteAccounts(request);

        // then
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(RESPONSE_CODE_BAD_REQUEST, result.get(0).getResponseCode());
    }

    @Test
    public void registerLiteAccounts_givenInvalidEmailFormat_thenReturnBadRequestCode() throws Exception {
        // given
        setProperties();
        List<LiteAccountDTO> request = Collections.singletonList(LiteAccountDTO.builder().email("test-email").build());
    
        // when
        List<EECUserV3> result = liteRegistrationService.registerLiteAccounts(request);
    
        // then
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(RESPONSE_CODE_BAD_REQUEST, result.get(0).getResponseCode());
    }

    @Test
    public void registerLiteAccounts_givenUnexpectedException_thenReturnGenericErrorCode() throws Exception {
        // given
        setProperties();
        when( gigyaService.searchInBothDC(MOCKED_EMAIL_1)).thenThrow(RuntimeException.class);

        List<LiteAccountDTO> request = Collections.singletonList(LiteAccountDTO.builder().email(MOCKED_EMAIL_1).build());

        // when
        List<EECUserV3> result = liteRegistrationService.registerLiteAccounts(request);

        // then
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(RESPONSE_CODE_GENERIC_ERROR, result.get(0).getResponseCode());
    }

    @Test
    public void registerLiteAccounts_givenCustomGigyaException_thenReturnGigyaErrorCode() throws Exception {
        // given
        setProperties();
        when( gigyaService.searchInBothDC(MOCKED_EMAIL_1)).thenThrow(new CustomGigyaErrorException(MOCKED_GIGYA_ERROR_MSG, MOCKED_GIGYA_ERROR_CODE));

        List<LiteAccountDTO> request = Collections.singletonList(LiteAccountDTO.builder().email(MOCKED_EMAIL_1).build());

        // when
        List<EECUserV3> result = liteRegistrationService.registerLiteAccounts(request);

        // then
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(MOCKED_GIGYA_ERROR_CODE, result.get(0).getResponseCode());
        Assertions.assertEquals(MOCKED_GIGYA_ERROR_MSG, result.get(0).getResponseMessage());
    }

    @Test
    public void registerLiteAccounts_givenAnExistingAccount_thenReturnAccountAlreadyExists() throws IllegalArgumentException, CustomGigyaErrorException, IOException {
        // given
        setProperties();
        when( gigyaService.searchInBothDC(MOCKED_EMAIL_1))
        .thenReturn(
            SearchResponse.builder()
                .cdcSearchResponse(
                    CDCSearchResponse.builder()
                        .results(Collections.singletonList(
                            CDCAccount.builder().UID(MOCKED_UID).profile(Profile.builder().email(MOCKED_EMAIL_1).build()).build()))
                    .build()
                )
            .build()
        );
        
        LiteAccountDTO account = LiteAccountDTO.builder()
                .email(MOCKED_EMAIL_1)
                .build();

        List<LiteAccountDTO> request = Collections.singletonList(account);

        // when
        List<EECUserV3> result = liteRegistrationService.registerLiteAccounts(request);

        // then
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(MOCKED_EMAIL_1, result.get(0).getEmail());
        Assertions.assertEquals(RESPONSE_CODE_ACCOUNT_AREADY_EXISTS, result.get(0).getResponseCode());
    }

    @Test
    public void registerLiteAccount_WhenAccountsListIsEmpty_ThenThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            liteRegistrationService.registerLiteAccounts(Collections.emptyList());
        });
    }

    @Test
    public void registerLiteAccount_WhenAccountsListExceedsMaxLimit_ThenThrowException() {
        // given
        setProperties();
        List<LiteAccountDTO> request = Arrays.asList(
            LiteAccountDTO.builder().build(),
            LiteAccountDTO.builder().build(),
            LiteAccountDTO.builder().build(),
            LiteAccountDTO.builder().build()
        );

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            liteRegistrationService.registerLiteAccounts(request);
        });
    }
}
