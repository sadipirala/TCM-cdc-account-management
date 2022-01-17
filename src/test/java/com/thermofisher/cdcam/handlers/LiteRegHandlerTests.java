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

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EECUserV1;
import com.thermofisher.cdcam.model.EECUserV2;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.model.cdc.CDCSearchResponse;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.Profile;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;

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

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class LiteRegHandlerTests {
    private final String ERROR_MSG = "Something went wrong, please contact the system administrator.";
    private final String uid = "59b44e6023214be5846c9cbd4cedfe93";

    @InjectMocks
    LiteRegHandler liteRegHandler;

    @Mock
    CDCResponseHandler cdcResponseHandler;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLiteAccountsV2_GivenTheEmailListContainsAnyNullEmail_ThrowIllegalArgumentException() throws IOException {
        // given
        ArrayList<String> emails = new ArrayList<String>();
        emails.add(null);
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        liteRegHandler.createLiteAccountsV2(emailList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLiteAccountsV2_GivenTheEmailListContainsAnyEmptyEmail_ThrowIllegalArgumentException() throws IOException {
        // given
        ArrayList<String> emails = new ArrayList<String>();
        emails.add("");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        liteRegHandler.createLiteAccountsV2(emailList);
    }

    @Test
    public void createLiteAccountsV2_givenEmailListEmpty_returnEmptyEECUserList() throws IOException {
        // given
        EmailList emailList = EmailList.builder().emails(new ArrayList<>()).build();

        // when
        List<EECUser> output = liteRegHandler.createLiteAccountsV2(emailList);

        // then
        Assert.assertTrue(output.isEmpty());
    }

    @Test
    public void createLiteAccountsV2_givenAnEmailListIsPassed_ThenTheResultShouldContainTheSameAmountOfItems() throws IOException, CustomGigyaErrorException {
        // given
        final Boolean isActive = false;
        final Boolean isRegistered = false;
        CDCAccount account = CDCAccount.builder().build();
        account.setUID(uid);
        account.setIsActive(isActive);
        account.setIsRegistered(isRegistered);
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
        List<EECUser> result = liteRegHandler.createLiteAccountsV2(emailList);

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
        CDCAccount account = CDCAccount.builder().build();
        account.setUID(uid);
        account.setProfile(profile);
        account.setIsActive(isActive);
        account.setIsRegistered(isRegistered);
        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        accounts.add(account);
        CDCSearchResponse searchResponse = new CDCSearchResponse();
        searchResponse.setResults(accounts);
        when(cdcResponseHandler.search(anyString(), any(), any())).thenReturn(searchResponse);
        when(cdcResponseHandler.searchInBothDC(anyString())).thenReturn(searchResponse);

        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> result = liteRegHandler.createLiteAccountsV2(emailList);
        
        // then
        EECUserV2 userV2 = (EECUserV2) result.get(0);
        assertEquals(uid, userV2.getUid());
        assertEquals(username, userV2.getUsername());
        assertEquals(isRegistered, userV2.getIsRegistered());
        assertEquals(isActive, userV2.getIsActive());
        assertEquals(isAvailable, userV2.getIsAvailable());
    }

    @Test
    public void createLiteAccountsV2_givenEmailIsNotFound_ThenItShouldBeLiteRegisteredAndOnlyContainItsUID_IsRegisteredAsFalse_AndIsActiveAsFalse() throws IOException, CustomGigyaErrorException {
        // given
        final Boolean isActive = true;
        final Boolean isRegistered = true;
        final Boolean isAvailable = true;
        final String username = "test@mail.com";
        Profile profile = Profile.builder().username(username).build();
        CDCAccount account = CDCAccount.builder().build();
        account.setUID(uid);
        account.setProfile(profile);
        account.setIsActive(isActive);
        account.setIsRegistered(isRegistered);

        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        CDCSearchResponse searchResponse = new CDCSearchResponse();
        searchResponse.setResults(accounts);
        when(cdcResponseHandler.search(anyString(), any(), any())).thenReturn(searchResponse);
        when(cdcResponseHandler.searchInBothDC(anyString())).thenReturn(searchResponse);

        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID(uid);
        when(cdcResponseHandler.liteRegisterUser(anyString())).thenReturn(cdcResponseData);

        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> output = liteRegHandler.createLiteAccountsV2(emailList);
        
        // then
        EECUserV2 userV2 = (EECUserV2) output.get(0);
        assertEquals(uid, userV2.getUid());
        assertNull(userV2.getUsername());
        assertFalse(userV2.getIsRegistered());
        assertFalse(userV2.getIsActive());
        assertEquals(isAvailable, userV2.getIsAvailable());
        verify(cdcResponseHandler).liteRegisterUser(anyString());
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
        List<EECUser> output = liteRegHandler.createLiteAccountsV2(emailList);

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
        List<EECUser> output = liteRegHandler.createLiteAccountsV2(emailList);

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
        liteRegHandler.createLiteAccountsV1(emailList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLiteAccountsV1_GivenTheEmailListContainsAnyEmptyEmail_ThrowIllegalArgumentException() throws IOException {
        // given
        ArrayList<String> emails = new ArrayList<String>();
        emails.add("");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        liteRegHandler.createLiteAccountsV1(emailList);
    }

    @Test
    public void createLiteAccountsV1_givenEmailListEmpty_returnEmptyEECUserList() throws IOException {
        // given
        EmailList emailList = EmailList.builder().emails(new ArrayList<>()).build();

        // when
        List<EECUser> output = liteRegHandler.createLiteAccountsV1(emailList);

        // then
        Assert.assertTrue(output.isEmpty());
    }

    @Test
    public void createLiteAccountsV1_givenAnEmailListIsPassed_ThenTheResultShouldContainTheSameAmountOfItems() throws IOException, CustomGigyaErrorException {
        // given
        final Boolean isActive = false;
        final Boolean isRegistered = false;
        CDCAccount account = CDCAccount.builder().build();
        account.setUID(uid);
        account.setIsActive(isActive);
        account.setIsRegistered(isRegistered);
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
        List<EECUser> result = liteRegHandler.createLiteAccountsV1(emailList);

        // then
        assertEquals(result.size(), emails.size());
    }

    @Test
    public void createLiteAccountsV1_givenAnEmailAlreadyExists_ThenResultShouldContainTheFoundAccountData() throws IOException, CustomGigyaErrorException {
        // given
        final Boolean isActive = true;
        final Boolean isRegistered = true;
        final Boolean isAvailable = false;
        final String username = "test@mail.com";
        Profile profile = Profile.builder().username(username).build();
        CDCAccount account = CDCAccount.builder().build();
        account.setUID(uid);
        account.setProfile(profile);
        account.setIsActive(isActive);
        account.setIsRegistered(isRegistered);
        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        accounts.add(account);
        CDCSearchResponse searchResponse = new CDCSearchResponse();
        searchResponse.setResults(accounts);
        when(cdcResponseHandler.searchInBothDC(anyString())).thenReturn(searchResponse);

        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> result = liteRegHandler.createLiteAccountsV1(emailList);
        
        // then
        EECUserV1 userV1 = (EECUserV1) result.get(0);
        assertEquals(uid, userV1.getUid());
        assertEquals(username, userV1.getUsername());
        assertEquals(isRegistered, userV1.getRegistered());
        assertEquals(isAvailable, userV1.getIsAvailable());
    }

    @Test
    public void createLiteAccountsV1_givenEmailIsNotFound_ThenItShouldBeLiteRegisteredAndOnlyContainItsUID_RegisteredAsFalse() throws IOException, CustomGigyaErrorException {
        // given
        final Boolean isActive = true;
        final Boolean isRegistered = true;
        final Boolean isAvailable = true;
        final String username = "test@mail.com";
        Profile profile = Profile.builder().username(username).build();
        CDCAccount account = CDCAccount.builder().build();
        account.setUID(uid);
        account.setProfile(profile);
        account.setIsActive(isActive);
        account.setIsRegistered(isRegistered);

        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        CDCSearchResponse searchResponse = new CDCSearchResponse();
        searchResponse.setResults(accounts);
        when(cdcResponseHandler.searchInBothDC(anyString())).thenReturn(searchResponse);

        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID(uid);
        when(cdcResponseHandler.liteRegisterUser(anyString())).thenReturn(cdcResponseData);

        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> output = liteRegHandler.createLiteAccountsV1(emailList);
        
        // then
        EECUserV1 userV1 = (EECUserV1) output.get(0);
        assertEquals(uid, userV1.getUid());
        assertNull(userV1.getUsername());
        assertFalse(userV1.getRegistered());
        assertEquals(isAvailable, userV1.getIsAvailable());
        verify(cdcResponseHandler).liteRegisterUser(anyString());
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
        List<EECUser> output = liteRegHandler.createLiteAccountsV1(emailList);

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
        List<EECUser> output = liteRegHandler.createLiteAccountsV1(emailList);

        // then
        EECUser user = output.get(0);
        assertNull(user.getUid());
        assertEquals(ERROR_MSG, user.getResponseMessage());
        assertEquals(500, user.getResponseCode());
    }
}
