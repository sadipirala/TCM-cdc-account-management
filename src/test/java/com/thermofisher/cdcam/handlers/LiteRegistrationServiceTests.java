package com.thermofisher.cdcam.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gigya.socialize.GSKeyNotFoundException;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.enums.ResponseCode;
import com.thermofisher.cdcam.enums.cdc.DataCenter;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EECUserV1;
import com.thermofisher.cdcam.model.EECUserV2;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CDCSearchResponse;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.Profile;
import com.thermofisher.cdcam.model.cdc.SearchResponse;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;
import com.thermofisher.cdcam.utils.cdc.LiteRegistrationService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class LiteRegistrationServiceTests {
    private final String ERROR_MSG = "Something went wrong, please contact the system administrator.";
    private final String uid = "59b44e6023214be5846c9cbd4cedfe93";

    @InjectMocks
    LiteRegistrationService liteRegistrationService;

    @Mock
    CDCResponseHandler cdcResponseHandler;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private void setProperties() {
        ReflectionTestUtils.setField(liteRegistrationService, "mainDataCenterName", "us");
    }

    private CDCAccount buildAccount(String uid, boolean isActive, boolean isRegistered, Profile profile) {
        return CDCAccount.builder()
        .UID(uid)
        .isActive(isActive)
        .isRegistered(isRegistered)
        .profile(profile)
        .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLiteAccountsV2_GivenTheEmailListContainsAnyNullEmail_ThrowIllegalArgumentException() throws IOException {
        // given
        ArrayList<String> emails = new ArrayList<String>();
        emails.add(null);
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        liteRegistrationService.registerEmailAccounts(emailList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLiteAccountsV2_GivenTheEmailListContainsAnyEmptyEmail_ThrowIllegalArgumentException() throws IOException {
        // given
        ArrayList<String> emails = new ArrayList<String>();
        emails.add("");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        liteRegistrationService.registerEmailAccounts(emailList);
    }

    @Test
    public void createLiteAccountsV2_givenEmailListEmpty_returnEmptyEECUserList() throws IOException {
        // given
        EmailList emailList = EmailList.builder().emails(new ArrayList<>()).build();

        // when
        List<EECUserV2> output = liteRegistrationService.registerEmailAccounts(emailList);

        // then
        Assert.assertTrue(output.isEmpty());
    }

    @Test
    public void createLiteAccountsV2_givenAnEmailListIsPassed_ThenTheResultShouldContainTheSameAmountOfItems() throws IOException, CustomGigyaErrorException {
        // given
        final Boolean isActive = false;
        final Boolean isRegistered = false;
        CDCAccount account = buildAccount(uid, isActive, isRegistered, null);
        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        accounts.add(account);
        CDCSearchResponse searchResponse = new CDCSearchResponse();
        searchResponse.setResults(accounts);
        when(cdcResponseHandler.search(anyString(), any(), anyString())).thenReturn(searchResponse);

        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        emails.add("test2");
        emails.add("test3");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUserV2> result = liteRegistrationService.registerEmailAccounts(emailList);

        // then
        assertEquals(result.size(), emails.size());
    }

    @Test
    public void createLiteAccountsV2_givenAnEmailAlreadyExists_ThenResultShouldContainTheFoundAccountData() throws IOException, CustomGigyaErrorException {
        // given
        final Boolean isActive = true;
        final Boolean isRegistered = true;
        final Boolean isAvailable = false;
        final String username = "test@mail.com";
        Profile profile = Profile.builder().username(username).build();
        CDCAccount account = buildAccount(uid, isActive, isRegistered, profile);
        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        accounts.add(account);
        CDCSearchResponse cdcSearchResponse = new CDCSearchResponse();
        cdcSearchResponse.setResults(accounts);
        SearchResponse searchResponse = SearchResponse.builder().cdcSearchResponse(cdcSearchResponse).dataCenter(DataCenter.US).build();
        when(cdcResponseHandler.search(anyString(), any(), any())).thenReturn(cdcSearchResponse);
        when(cdcResponseHandler.searchInBothDC(anyString())).thenReturn(searchResponse);

        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUserV2> result = liteRegistrationService.registerEmailAccounts(emailList);
        
        // then
        EECUserV2 userV2 = (EECUserV2) result.get(0);
        assertEquals(uid, userV2.getUid());
        assertEquals(username, userV2.getUsername());
        assertEquals(isRegistered, userV2.getIsRegistered());
        assertEquals(isActive, userV2.getIsActive());
        assertEquals(isAvailable, userV2.getIsAvailable());
    }

    @Test
    public void createLiteAccountsV2_givenEmailIsNotFound_ThenItShouldBeLiteRegisteredAndOnlyContainItsUID_IsRegisteredAsFalse_AndIsActiveAsFalse() throws IOException, CustomGigyaErrorException, GSKeyNotFoundException {
        // given
        final Boolean isAvailable = true;

        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        CDCSearchResponse cdcSearchResponse = new CDCSearchResponse();
        cdcSearchResponse.setResults(accounts);
        SearchResponse searchResponse = SearchResponse.builder().cdcSearchResponse(cdcSearchResponse).dataCenter(DataCenter.US).build();
        when(cdcResponseHandler.search(anyString(), any(), any())).thenReturn(cdcSearchResponse);
        when(cdcResponseHandler.searchInBothDC(anyString())).thenReturn(searchResponse);

        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID(uid);
        when(cdcResponseHandler.registerLiteAccount(anyString())).thenReturn(cdcResponseData);

        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUserV2> output = liteRegistrationService.registerEmailAccounts(emailList);
        
        // then
        EECUserV2 userV2 = (EECUserV2) output.get(0);
        assertEquals(uid, userV2.getUid());
        assertNull(userV2.getUsername());
        assertFalse(userV2.getIsRegistered());
        assertFalse(userV2.getIsActive());
        assertEquals(isAvailable, userV2.getIsAvailable());
        verify(cdcResponseHandler).registerLiteAccount(anyString());
    }

    @Test
    public void createLiteAccountsV2_givenSearchThrowsCustomGigyaErrorException_returnEECUserWith500Error() throws IOException, CustomGigyaErrorException {
        // given
        final String errorMessage = "Error";
        final int errorCode = 400;

        when(cdcResponseHandler.searchInBothDC(anyString())).thenThrow(new CustomGigyaErrorException(errorMessage, errorCode));

        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUserV2> output = liteRegistrationService.registerEmailAccounts(emailList);

        // then
        EECUser user = output.get(0);
        assertEquals(errorMessage, user.getResponseMessage());
        assertEquals(errorCode, user.getResponseCode());
    }

    @Test
    public void createLiteAccountsV2_givenSearchThrowsAnyOtherException_returnEECUserWith500Error() throws IOException, CustomGigyaErrorException {
        // given
        when(cdcResponseHandler.search(anyString(), any(), anyString())).thenThrow(new IOException());
        
        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUserV2> output = liteRegistrationService.registerEmailAccounts(emailList);

        // then
        EECUser user = output.get(0);
        assertNull(user.getUid());
        assertEquals(ERROR_MSG, user.getResponseMessage());
        assertEquals(500, user.getResponseCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLiteAccountsV1_GivenTheEmailListContainsAnyNullEmail_ThrowIllegalArgumentException() throws IOException {
        // given
        ArrayList<String> emails = new ArrayList<String>();
        emails.add(null);
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        liteRegistrationService.createLiteAccountsV1(emailList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLiteAccountsV1_GivenTheEmailListContainsAnyEmptyEmail_ThrowIllegalArgumentException() throws IOException {
        // given
        ArrayList<String> emails = new ArrayList<String>();
        emails.add("");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        liteRegistrationService.createLiteAccountsV1(emailList);
    }

    @Test
    public void createLiteAccountsV1_givenEmailListEmpty_returnEmptyEECUserList() throws IOException {
        // given
        EmailList emailList = EmailList.builder().emails(new ArrayList<>()).build();

        // when
        List<EECUser> output = liteRegistrationService.createLiteAccountsV1(emailList);

        // then
        Assert.assertTrue(output.isEmpty());
    }

    @Test
    public void createLiteAccountsV1_givenAnEmailListIsPassed_ThenTheResultShouldContainTheSameAmountOfItems() throws IOException, CustomGigyaErrorException {
        // given
        final Boolean isActive = false;
        final Boolean isRegistered = false;
        CDCAccount account = buildAccount(uid, isActive, isRegistered, null);

        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        accounts.add(account);
        CDCSearchResponse searchResponse = new CDCSearchResponse();
        searchResponse.setResults(accounts);
        when(cdcResponseHandler.search(anyString(), any(), anyString())).thenReturn(searchResponse);

        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        emails.add("test2");
        emails.add("test3");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> result = liteRegistrationService.createLiteAccountsV1(emailList);

        // then
        assertEquals(result.size(), emails.size());
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
        CDCAccount account = buildAccount(uid, isActive, isRegistered, profile);

        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        accounts.add(account);
        CDCSearchResponse cdcSearchResponse = new CDCSearchResponse();
        cdcSearchResponse.setResults(accounts);
        SearchResponse searchResponse = SearchResponse.builder().cdcSearchResponse(cdcSearchResponse).dataCenter(DataCenter.US).build();
        when(cdcResponseHandler.search(anyString(), any(), any())).thenReturn(cdcSearchResponse);
        when(cdcResponseHandler.searchInBothDC(anyString())).thenReturn(searchResponse);

        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> result = liteRegistrationService.createLiteAccountsV1(emailList);
        
        // then
        EECUserV1 userV1 = (EECUserV1) result.get(0);
        assertEquals(uid, userV1.getUid());
        assertEquals(username, userV1.getUsername());
        assertEquals(isRegistered, userV1.getRegistered());
        assertEquals(isAvailable, userV1.getIsAvailable());
    }

    @Test
    public void createLiteAccountsV1_givenEmailIsNotFound_ThenItShouldBeLiteRegisteredAndOnlyContainItsUID_RegisteredAsFalse() throws IOException, CustomGigyaErrorException, GSKeyNotFoundException {
        // given
        final Boolean isAvailable = true;
        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        CDCSearchResponse cdcSearchResponse = new CDCSearchResponse();
        cdcSearchResponse.setResults(accounts);
        SearchResponse searchResponse = SearchResponse.builder().cdcSearchResponse(cdcSearchResponse).dataCenter(DataCenter.US).build();
        when(cdcResponseHandler.search(anyString(), any(), any())).thenReturn(cdcSearchResponse);
        when(cdcResponseHandler.searchInBothDC(anyString())).thenReturn(searchResponse);

        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID(uid);
        when(cdcResponseHandler.registerLiteAccount(anyString())).thenReturn(cdcResponseData);

        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> output = liteRegistrationService.createLiteAccountsV1(emailList);
        
        // then
        EECUserV1 userV1 = (EECUserV1) output.get(0);
        assertEquals(uid, userV1.getUid());
        assertNull(userV1.getUsername());
        assertFalse(userV1.getRegistered());
        assertEquals(isAvailable, userV1.getIsAvailable());
        verify(cdcResponseHandler).registerLiteAccount(anyString());
    }

    @Test
    public void createLiteAccountsV1_givenSearchThrowsCustomGigyaErrorException_returnEECUserWith500Error() throws IOException, CustomGigyaErrorException {
        // given
        final String errorMessage = "Error";
        final int errorCode = 400;

        when(cdcResponseHandler.searchInBothDC(anyString())).thenThrow(new CustomGigyaErrorException(errorMessage, errorCode));

        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> output = liteRegistrationService.createLiteAccountsV1(emailList);

        // then
        EECUser user = output.get(0);
        assertEquals(errorMessage, user.getResponseMessage());
        assertEquals(errorCode, user.getResponseCode());
    }

    @Test
    public void createLiteAccountsV1_givenSearchThrowsAnyOtherException_returnEECUserWith500Error() throws IOException, CustomGigyaErrorException {
        // given
        when(cdcResponseHandler.search(anyString(), any(), anyString())).thenThrow(new IOException());
        
        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> output = liteRegistrationService.createLiteAccountsV1(emailList);

        // then
        EECUser user = output.get(0);
        assertNull(user.getUid());
        assertEquals(ERROR_MSG, user.getResponseMessage());
        assertEquals(500, user.getResponseCode());
    }

    @Test
    public void createLiteAccountsV1_givenAnEmailAlreadyExists_ThenResponseCodeShouldBeSuccess() throws IOException, CustomGigyaErrorException {
        // given
        setProperties();
        final Boolean isActive = true;
        final Boolean isRegistered = true;
        final String username = "test@mail.com";
        Profile profile = Profile.builder().username(username).build();
        CDCAccount account = buildAccount(uid, isActive, isRegistered, profile);

        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        accounts.add(account);
        CDCSearchResponse cdcSearchResponse = new CDCSearchResponse();
        cdcSearchResponse.setResults(accounts);
        SearchResponse searchResponse = SearchResponse.builder().cdcSearchResponse(cdcSearchResponse).dataCenter(DataCenter.US).build();
        when(cdcResponseHandler.search(anyString(), any(), any())).thenReturn(cdcSearchResponse);
        when(cdcResponseHandler.searchInBothDC(anyString())).thenReturn(searchResponse);

        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> result = liteRegistrationService.createLiteAccountsV1(emailList);
        
        // then
        EECUserV1 userV1 = (EECUserV1) result.get(0);
        assertEquals(ResponseCode.SUCCESS.getValue(), userV1.getResponseCode());
    }
}
