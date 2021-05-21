package com.thermofisher.cdcam;

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
    public void createLiteAccounts_GivenTheEmailListContainsAnyNullEmail_ThrowIllegalArgumentException() throws IOException {
        // given
        ArrayList<String> emails = new ArrayList<String>();
        emails.add(null);
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        liteRegHandler.createLiteAccounts(emailList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLiteAccounts_GivenTheEmailListContainsAnyEmptyEmail_ThrowIllegalArgumentException() throws IOException {
        // given
        ArrayList<String> emails = new ArrayList<String>();
        emails.add("");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        liteRegHandler.createLiteAccounts(emailList);
    }

    @Test
    public void createLiteAccounts_givenEmailListEmpty_returnEmptyEECUserList() throws IOException {
        // given
        EmailList emailList = EmailList.builder().emails(new ArrayList<>()).build();

        // when
        List<EECUser> output = liteRegHandler.createLiteAccounts(emailList);

        // then
        Assert.assertTrue(output.isEmpty());
    }

    @Test
    public void createLiteAccounts_givenAnEmailListIsPassed_ThenTheResultShouldContainTheSameAmountOfItems() throws IOException, CustomGigyaErrorException {
        // given
        final Boolean isActive = false;
        final Boolean isRegistered = false;
        CDCAccount account = new CDCAccount();
        account.setUID(uid);
        account.setIsActive(isActive);
        account.setIsRegistered(isRegistered);
        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        accounts.add(account);
        CDCSearchResponse searchResponse = new CDCSearchResponse();
        searchResponse.setResults(accounts);
        when(cdcResponseHandler.search(anyString(), any())).thenReturn(searchResponse);

        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        emails.add("test2");
        emails.add("test3");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> result = liteRegHandler.createLiteAccounts(emailList);

        // then
        assertEquals(result.size(), emails.size());
    }

    @Test
    public void createLiteAccounts_givenAnEmailAlreadyExists_ThenResultShouldContainTheFoundAccountData() throws IOException, CustomGigyaErrorException {
        // given
        final Boolean isActive = true;
        final Boolean isRegistered = true;
        final String username = "test@mail.com";
        Profile profile = Profile.builder().username(username).build();
        CDCAccount account = new CDCAccount();
        account.setUID(uid);
        account.setProfile(profile);
        account.setIsActive(isActive);
        account.setIsRegistered(isRegistered);
        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        accounts.add(account);
        CDCSearchResponse searchResponse = new CDCSearchResponse();
        searchResponse.setResults(accounts);
        when(cdcResponseHandler.search(anyString(), any())).thenReturn(searchResponse);
        
        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> result = liteRegHandler.createLiteAccounts(emailList);
        
        // then
        EECUser user = result.get(0);
        assertEquals(uid, user.getUid());
        assertEquals(username, user.getUsername());
        assertEquals(isRegistered, user.getRegistered());
        // assertEquals(isActive, user.getIsActive());
    }

    @Test
    public void createLiteAccounts_givenEmailIsNotFound_ThenItShouldBeLiteRegisteredAndOnlyContainItsUID_IsRegisteredAsFalse_AndIsActiveAsFalse() throws IOException, CustomGigyaErrorException {
        // given
        final Boolean isActive = true;
        final Boolean isRegistered = true;
        final String username = "test@mail.com";
        Profile profile = Profile.builder().username(username).build();
        CDCAccount account = new CDCAccount();
        account.setUID(uid);
        account.setProfile(profile);
        account.setIsActive(isActive);
        account.setIsRegistered(isRegistered);

        List<CDCAccount> accounts = new ArrayList<CDCAccount>();
        CDCSearchResponse searchResponse = new CDCSearchResponse();
        searchResponse.setResults(accounts);
        when(cdcResponseHandler.search(anyString(), any())).thenReturn(searchResponse);

        CDCResponseData cdcResponseData = new CDCResponseData();
        cdcResponseData.setUID(uid);
        when(cdcResponseHandler.liteRegisterUser(anyString())).thenReturn(cdcResponseData);

        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> output = liteRegHandler.createLiteAccounts(emailList);
        
        // then
        EECUser user = output.get(0);
        assertEquals(uid, user.getUid());
        assertNull(user.getUsername());
        assertFalse(user.getRegistered());
        // assertFalse(user.getIsActive());
        verify(cdcResponseHandler).liteRegisterUser(anyString());
    }

    @Test
    public void createLiteAccounts_givenSearchThrowsCustomGigyaErrorException_returnEECUserWith500Error() throws IOException, CustomGigyaErrorException {
        // given
        final String errorMessage = "Error";
        final int errorCode = 400;
        when(cdcResponseHandler.search(anyString(), any())).thenThrow(new CustomGigyaErrorException(errorMessage, errorCode));
        
        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> output = liteRegHandler.createLiteAccounts(emailList);

        // then
        EECUser user = output.get(0);
        assertEquals(errorMessage, user.getResponseMessage());
        assertEquals(errorCode, user.getResponseCode());
    }

    @Test
    public void createLiteAccounts_givenSearchThrowsAnyOtherException_returnEECUserWith500Error() throws IOException, CustomGigyaErrorException {
        // given
        when(cdcResponseHandler.search(anyString(), any())).thenThrow(new IOException());
        
        List<String> emails = new ArrayList<String>();
        emails.add("test1");
        EmailList emailList = EmailList.builder().emails(emails).build();

        // when
        List<EECUser> output = liteRegHandler.createLiteAccounts(emailList);

        // then
        EECUser user = output.get(0);
        assertNull(user.getUid());
        assertEquals(ERROR_MSG, user.getResponseMessage());
        assertEquals(500, user.getResponseCode());
    }
}
