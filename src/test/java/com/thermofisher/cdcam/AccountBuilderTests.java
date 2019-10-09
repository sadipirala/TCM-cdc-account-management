package com.thermofisher.cdcam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigya.socialize.GSObject;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.model.AccountInfo;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AccountBuilder.class)
public class AccountBuilderTests {

    @Mock
    AccountBuilder accountBuilder = new AccountBuilder();

    private final String uid = "c1c691f4-556b-4ad1-ab75-841fc4e94dcd";
    private final String username = "federatedUser@OIDC.com";
    private final String emailAddress = "federatedUser@OIDC.com";
    private final String firstName = "first";
    private final String lastName = "first";
    private final String country = "United States";
    private final String localeName = "en_US";
    private final String loginProvider = "oidc";
    private final String password = "Password1";
    private final String city = "testCity";
    private final String department = "dep";
    private final String company = "company";

    private String obj = "{\"socialProviders\":\"site,oidc-fedspikegidp\",\"lastLogin\":\"2019-08-21T23:13:38.284Z\",\"userInfo\":{\"country\":\"United States\",\"isTempUser\":false,\"oldestDataAge\":-2147483648,\"capabilities\":\"None\",\"isSiteUID\":true,\"loginProviderUID\":\"ef632aa3f52140aa836673469378d0ac\",\"city\":\"" + city + "\",\"isConnected\":true,\"errorCode\":0,\"isSiteUser\":true,\"loginProvider\":\"oidc-fedspikegidp\",\"oldestDataUpdatedTimestamp\":0,\"UID\":\"ffb10070d8174a518f2e8b403c1efe5d\",\"identities\":[{\"country\":\"United States\",\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"lastUpdatedTimestamp\":1566429217356,\"isExpiredSession\":false,\"allowsLogin\":false,\"city\":\"" + city + "\",\"provider\":\"site\",\"isLoginIdentity\":false,\"oldestDataUpdated\":\"0001-01-01T00:00:00Z\",\"oldestDataUpdatedTimestamp\":0,\"providerUID\":\"ffb10070d8174a518f2e8b403c1efe5d\"},{\"lastUpdated\":\"2019-08-21T23:13:38.284Z\",\"lastUpdatedTimestamp\":1566429218284,\"isExpiredSession\":false,\"allowsLogin\":true,\"provider\":\"oidc-fedspikegidp\",\"isLoginIdentity\":true,\"nickname\":\"federatedUser\",\"oldestDataUpdated\":\"2019-08-21T23:01:23.988Z\",\"oidcData\":{},\"oldestDataUpdatedTimestamp\":1566428483988,\"email\":\"test@gmail.com\",\"providerUID\":\"ef632aa3f52140aa836673469378d0ac\"}],\"statusReason\":\"OK\",\"nickname\":\"federatedUser\",\"isLoggedIn\":true,\"time\":\"2019-08-23T23:50:35.918Z\",\"email\":\"test@gmail.com\",\"providers\":\"site,oidc-fedspikegidp\",\"statusCode\":200},\"data\":{\"terms\":true},\"isVerified\":true,\"errorCode\":0,\"registered\":\"2019-08-19T21:11:52.372Z\",\"isActive\":true,\"oldestDataUpdatedTimestamp\":1566248846440,\"emails\":{\"verified\":[\"test@gmail.com\"],\"unverified\":[]},\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"apiVersion\":2,\"statusReason\":\"OK\",\"verifiedTimestamp\":1566248848104,\"oldestDataUpdated\":\"2019-08-19T21:07:26.440Z\",\"callId\":\"52317e98c0a849438f432669c5d198f0\",\"lastUpdatedTimestamp\":1566429217356,\"created\":\"2019-08-19T21:07:26.440Z\",\"createdTimestamp\":1566248846000,\"profile\":{\"firstName\":\"" + firstName + "\",\"lastName\":\"" + lastName + "\",\"work\":{\"company\":\"" + company + "\",\"location\":\"" + department + "\"},\"country\":\"" + country + "\",\"city\":\"" + city + "\",\"nickname\":\"federatedUser\",\"email\":\"" + emailAddress + "\"},\"regSource\":\"http://dev2.apps.thermofisher.com/apps/fedspike/enterpriselogin\",\"verified\":\"2019-08-19T21:07:28.104Z\",\"registeredTimestamp\":1566249112000,\"loginProvider\":\"oidc-fedspikegidp\",\"lastLoginTimestamp\":1566429218000,\"UID\":\"" + uid + "\",\"isRegistered\":true,\"time\":\"2019-08-23T23:50:35.919Z\",\"statusCode\":200}";
    private String invalidObj = "{\"socialProviders\":\"site,oidc-fedspikegidp\",\"lastLogin\":\"2019-08-21T23:13:38.284Z\",\"userInfo\":{\"country\":\"United States\",\"isTempUser\":false,\"oldestDataAge\":-2147483648,\"capabilities\":\"None\",\"isSiteUID\":true,\"loginProviderUID\":\"ef632aa3f52140aa836673469378d0ac\",\"city\":\"ted\",\"isConnected\":true,\"errorCode\":0,\"isSiteUser\":true,\"loginProvider\":\"oidc-fedspikegidp\",\"oldestDataUpdatedTimestamp\":0,\"UID\":\"ffb10070d8174a518f2e8b403c1efe5d\",\"identities\":[{\"country\":\"United States\",\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"lastUpdatedTimestamp\":1566429217356,\"isExpiredSession\":false,\"allowsLogin\":false,\"city\":\"ted\",\"provider\":\"site\",\"isLoginIdentity\":false,\"oldestDataUpdated\":\"0001-01-01T00:00:00Z\",\"oldestDataUpdatedTimestamp\":0,\"providerUID\":\"ffb10070d8174a518f2e8b403c1efe5d\"},{\"lastUpdated\":\"2019-08-21T23:13:38.284Z\",\"lastUpdatedTimestamp\":1566429218284,\"isExpiredSession\":false,\"allowsLogin\":true,\"provider\":\"oidc-fedspikegidp\",\"isLoginIdentity\":true,\"nickname\":\"federatedUser\",\"oldestDataUpdated\":\"2019-08-21T23:01:23.988Z\",\"oidcData\":{},\"oldestDataUpdatedTimestamp\":1566428483988,\"email\":\"test@gmail.com\",\"providerUID\":\"ef632aa3f52140aa836673469378d0ac\"}],\"statusReason\":\"OK\",\"nickname\":\"federatedUser\",\"isLoggedIn\":true,\"time\":\"2019-08-23T23:50:35.918Z\",\"email\":\"test@gmail.com\",\"providers\":\"site,oidc-fedspikegidp\",\"statusCode\":200},\"isVerified\":true,\"errorCode\":0,\"registered\":\"2019-08-19T21:11:52.372Z\",\"isActive\":true,\"oldestDataUpdatedTimestamp\":1566248846440,\"emails\":{\"verified\":[\"test@gmail.com\"],\"unverified\":[]},\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"apiVersion\":2,\"statusReason\":\"OK\",\"verifiedTimestamp\":1566248848104,\"oldestDataUpdated\":\"2019-08-19T21:07:26.440Z\",\"callId\":\"52317e98c0a849438f432669c5d198f0\",\"lastUpdatedTimestamp\":1566429217356,\"created\":\"2019-08-19T21:07:26.440Z\",\"createdTimestamp\":1566248846000,\"profile\":{\"country\":\"United States\",\"city\":\"ted\",\"nickname\":\"federatedUser\",\"email\":\"test@gmail.com\"},\"regSource\":\"http://dev2.apps.thermofisher.com/apps/fedspike/enterpriselogin\",\"verified\":\"2019-08-19T21:07:28.104Z\",\"registeredTimestamp\":1566249112000,\"loginProvider\":\"oidc-fedspikegidp\",\"lastLoginTimestamp\":1566429218000,\"UID\":\"ffb10070d8174a518f2e8b403c1efe5d\",\"isRegistered\":true,\"time\":\"2019-08-23T23:50:35.919Z\",\"statusCode\":200}";
    private AccountInfo toNotifyAccount = AccountInfo.builder()
            .uid(uid)
            .username(username)
            .firstName(firstName)
            .lastName(lastName)
            .emailAddress(emailAddress)
            .company(company)
            .department(department)
            .city(city)
            .country(country)
            .build();

    private AccountInfo federationAccount = AccountInfo.builder()
            .username(username)
            .emailAddress(emailAddress)
            .firstName(firstName)
            .lastName(lastName)
            .country(country)
            .localeName(localeName)
            .loginProvider(loginProvider)
            .password(password)
            .regAttempts(0)
            .city(city)
            .department(department)
            .company(company)
            .build();

    @Test
    public void getAccountInfo_ifGivenAnyUserInfoAndObj_returnAccountInfo() {
        // setup
        Mockito.when(accountBuilder.getAccountInfo(any(GSObject.class))).thenReturn(federationAccount);

        // execution
        AccountInfo res = accountBuilder.getAccountInfo(new GSObject());

        // validation
        Assert.assertEquals(res.getUid(), federationAccount.getUid());
        Assert.assertEquals(res.getUsername(), federationAccount.getUsername());
        Assert.assertEquals(res.getEmailAddress(), federationAccount.getEmailAddress());
        Assert.assertEquals(res.getFirstName(), federationAccount.getFirstName());
        Assert.assertEquals(res.getLastName(), federationAccount.getLastName());
        Assert.assertEquals(res.getCountry(), federationAccount.getCountry());
        Assert.assertEquals(res.getLocaleName(), federationAccount.getLocaleName());
        Assert.assertEquals(res.getLoginProvider(), federationAccount.getLoginProvider());
        Assert.assertEquals(res.getPassword(), federationAccount.getPassword());
        Assert.assertEquals(res.getRegAttempts(), federationAccount.getRegAttempts());
        Assert.assertEquals(res.getCity(), federationAccount.getCity());
        Assert.assertEquals(res.getDepartment(), federationAccount.getDepartment());
        Assert.assertEquals(res.getCompany(), federationAccount.getCompany());
    }

    @Test
    public void getAccountInfo_ifGivenUserInfoAndObj_returnAccountInfo() throws Exception {
        // setup
        Mockito.when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(obj);

        // execution
        AccountInfo res = accountBuilder.getAccountInfo(jsonObj);

        // validation
        Assert.assertEquals(res.getUid(), uid);
        Assert.assertEquals(res.getEmailAddress(), emailAddress);
    }

    @Test
    public void getAccountInfo_ifGivenAInvalidObj_returnNull() throws Exception {
        // setup
        Mockito.when(accountBuilder.getAccountInfo(any(GSObject.class))).thenCallRealMethod();
        GSObject jsonObj = new GSObject(invalidObj);

        // execution
        AccountInfo res = accountBuilder.getAccountInfo(jsonObj);

        // validation
        Assert.assertNull(res);
    }

    @Test
    public void getAccountToNotifyRegistration_ShouldReturnAccountInfoWithExpectedData() throws JsonProcessingException, Exception {
        // given
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String expectedAccountInfo = mapper.writeValueAsString(toNotifyAccount);
        when(accountBuilder.getAccountToNotifyRegistration(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo accountInfo = accountBuilder.getAccountToNotifyRegistration(new GSObject(obj));
        String _accountInfo = mapper.writeValueAsString(accountInfo);

        // then
        assertEquals(expectedAccountInfo, _accountInfo);
    }

    @Test
    public void getAccountToNotifyRegistration_WhenAnExceptionOccurs_ThenNullShouldBeReturned() throws Exception {
        // given
        when(accountBuilder.getAccountToNotifyRegistration(any(GSObject.class))).thenCallRealMethod();

        // when
        AccountInfo accountInfo = accountBuilder.getAccountToNotifyRegistration(new GSObject("{}"));

        // then
        assertNull(accountInfo);
    }
}
