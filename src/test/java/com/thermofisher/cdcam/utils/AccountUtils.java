package com.thermofisher.cdcam.utils;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.*;
import com.thermofisher.cdcam.model.dto.AccountInfoDTO;

import org.json.JSONException;

import lombok.Getter;

/**
 * AccountInfoUtils
 */
@Getter
public class AccountUtils {
    public static final String uid = "c1c691f4-556b-4ad1-ab75-841fc4e94dcd";
    public static final String federatedUsername = "federatedUser@OIDC.com";
    public static final String federatedEmailAddress = "federatedUser@OIDC.com";
    public static final String username = "siteUsername@mail.com";
    public static final String email = "siteUsername@mail.com";
    public static final String password = "Xh9s8Jml0";
    public static final String hash = "CXG+ERQHMHMOU/NQ0A/UKA==";
    public static final String algorithm = "MD5";
    public static final String reCaptchaToken = "c1c691f4556b4ad1ab75841fc4e94dcd";
    public static final String firstName = "first";
    public static final String lastName = "last";
    public static final String country = "United States";
    public static final String city = "testCity";
    public static final String department = "dep";
    public static final String member = "member";
    public static final String localeName = "en_US";
    public static final String loginProvider = "oidc";
    public static final String company = "company";
    public static final String location = "Digital Engineering";
    public static final String timezone = "America/Tijuana";
    public static final String hiraganaName = "ひらがな";
    public static final String jobRole = "Development";
    public static final String interest = "Test interest";
    public static final String phoneNumber = "6648675309";
    public static final String federatedCdcResponse = "{\"socialProviders\":\"site,oidc-fedspikegidp\",\"lastLogin\":\"2019-08-21T23:13:38.284Z\",\"userInfo\":{\"country\":\"United States\",\"isTempUser\":false,\"oldestDataAge\":-2147483648,\"capabilities\":\"None\",\"isSiteUID\":true,\"loginProviderUID\":\"ef632aa3f52140aa836673469378d0ac\",\"city\":\"" + city + "\",\"isConnected\":true,\"errorCode\":0,\"isSiteUser\":true,\"loginProvider\":\"" + loginProvider + "\",\"oldestDataUpdatedTimestamp\":0,\"UID\":\"" + uid + "\",\"identities\":[{\"country\":\"United States\",\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"lastUpdatedTimestamp\":1566429217356,\"isExpiredSession\":false,\"allowsLogin\":false,\"city\":\"" + city + "\",\"provider\":\"site\",\"isLoginIdentity\":false,\"oldestDataUpdated\":\"0001-01-01T00:00:00Z\",\"oldestDataUpdatedTimestamp\":0,\"providerUID\":\"ffb10070d8174a518f2e8b403c1efe5d\"},{\"lastUpdated\":\"2019-08-21T23:13:38.284Z\",\"lastUpdatedTimestamp\":1566429218284,\"isExpiredSession\":false,\"allowsLogin\":true,\"provider\":\"oidc-fedspikegidp\",\"isLoginIdentity\":true,\"nickname\":\"federatedUser\",\"oldestDataUpdated\":\"2019-08-21T23:01:23.988Z\",\"oidcData\":{},\"oldestDataUpdatedTimestamp\":1566428483988,\"email\":\"test@gmail.com\",\"providerUID\":\"ef632aa3f52140aa836673469378d0ac\"}],\"statusReason\":\"OK\",\"nickname\":\"federatedUser\",\"isLoggedIn\":true,\"time\":\"2019-08-23T23:50:35.918Z\",\"email\":\"test@gmail.com\",\"providers\":\"site,oidc-fedspikegidp\",\"statusCode\":200},\"data\":{\"subscribe\":\"" + member + "\",\"terms\":true},\"isVerified\":true,\"errorCode\":0,\"registered\":\"2019-08-19T21:11:52.372Z\",\"isActive\":true,\"oldestDataUpdatedTimestamp\":1566248846440,\"emails\":{\"verified\":[\"test@gmail.com\"],\"unverified\":[]},\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"apiVersion\":2,\"statusReason\":\"OK\",\"verifiedTimestamp\":1566248848104,\"oldestDataUpdated\":\"2019-08-19T21:07:26.440Z\",\"callId\":\"52317e98c0a849438f432669c5d198f0\",\"lastUpdatedTimestamp\":1566429217356,\"created\":\"2019-08-19T21:07:26.440Z\",\"createdTimestamp\":1566248846000,\"profile\":{\"locale\":\"" + localeName + "\",\"firstName\":\"" + firstName + "\",\"lastName\":\"" + lastName + "\",\"work\":{\"company\":\"" + company + "\",\"location\":\"" + department + "\"},\"country\":\"" + country + "\",\"city\":\"" + city + "\",\"nickname\":\"federatedUser\",\"email\":\"" + federatedEmailAddress + "\"},\"regSource\":\"http://dev2.apps.thermofisher.com/apps/fedspike/enterpriselogin\",\"verified\":\"2019-08-19T21:07:28.104Z\",\"registeredTimestamp\":1566249112000,\"loginProvider\":\"" + loginProvider + "\",\"lastLoginTimestamp\":1566429218000,\"UID\":\"" + uid + "\",\"isRegistered\":true,\"time\":\"2019-08-23T23:50:35.919Z\",\"statusCode\":200}";
    public static final String siteUserCdcResponse = "{\"socialProviders\":\"site\",\"lastLogin\":\"2019-08-21T23:13:38.284Z\",\"userInfo\":{\"country\":\"United States\",\"isTempUser\":false,\"oldestDataAge\":-2147483648,\"capabilities\":\"None\",\"isSiteUID\":true,\"loginProviderUID\":\"ef632aa3f52140aa836673469378d0ac\",\"city\":\"" + city + "\",\"isConnected\":true,\"errorCode\":0,\"isSiteUser\":true,\"loginProvider\":\"" + loginProvider + "\",\"oldestDataUpdatedTimestamp\":0,\"UID\":\"" + uid + "\",\"identities\":[{\"country\":\"United States\",\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"lastUpdatedTimestamp\":1566429217356,\"isExpiredSession\":false,\"allowsLogin\":false,\"city\":\"" + city + "\",\"provider\":\"site\",\"isLoginIdentity\":false,\"oldestDataUpdated\":\"0001-01-01T00:00:00Z\",\"oldestDataUpdatedTimestamp\":0,\"providerUID\":\"ffb10070d8174a518f2e8b403c1efe5d\"},{\"lastUpdated\":\"2019-08-21T23:13:38.284Z\",\"lastUpdatedTimestamp\":1566429218284,\"isExpiredSession\":false,\"allowsLogin\":true,\"provider\":\"oidc-fedspikegidp\",\"isLoginIdentity\":true,\"nickname\":\"federatedUser\",\"oldestDataUpdated\":\"2019-08-21T23:01:23.988Z\",\"oidcData\":{},\"oldestDataUpdatedTimestamp\":1566428483988,\"email\":\"test@gmail.com\",\"providerUID\":\"ef632aa3f52140aa836673469378d0ac\"}],\"statusReason\":\"OK\",\"nickname\":\"federatedUser\",\"isLoggedIn\":true,\"time\":\"2019-08-23T23:50:35.918Z\",\"email\":\"test@gmail.com\",\"providers\":\"site,oidc-fedspikegidp\",\"statusCode\":200},\"data\":{\"subscribe\":\"" + member + "\",\"terms\":true, \"registration\":{\"china\":{\"interest\": \"" + interest + "\",\"jobRole\": \"" + jobRole + "\",\"phoneNumber\": \"" + phoneNumber + "\"},\"japan\":{\"hiraganaName\": \"" + hiraganaName + "\"}}},\"isVerified\":true,\"errorCode\":0,\"registered\":\"2019-08-19T21:11:52.372Z\",\"isActive\":true,\"oldestDataUpdatedTimestamp\":1566248846440,\"emails\":{\"verified\":[\"test@gmail.com\"],\"unverified\":[]},\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"apiVersion\":2,\"statusReason\":\"OK\",\"verifiedTimestamp\":1566248848104,\"oldestDataUpdated\":\"2019-08-19T21:07:26.440Z\",\"callId\":\"52317e98c0a849438f432669c5d198f0\",\"lastUpdatedTimestamp\":1566429217356,\"created\":\"2019-08-19T21:07:26.440Z\",\"createdTimestamp\":1566248846000,\"password\":{\"hash\":\"" + hash + "\",\"hashSettings\":{\"algorithm\":\"" + algorithm + "\"}},\"profile\":{\"locale\":\"" + localeName + "\",\"firstName\":\"" + firstName + "\",\"lastName\":\"" + lastName + "\",\"work\":{\"company\":\"" + company + "\",\"location\":\"" + department + "\"},\"country\":\"" + country + "\",\"city\":\"" + city + "\",\"nickname\":\"siteUser\",\"email\":\"" + email + "\"},\"regSource\":\"http://dev2.apps.thermofisher.com/apps/fedspike/enterpriselogin\",\"verified\":\"2019-08-19T21:07:28.104Z\",\"registeredTimestamp\":1566249112000,\"loginProvider\":\"" + loginProvider + "\",\"lastLoginTimestamp\":1566429218000,\"UID\":\"" + uid + "\",\"isRegistered\":true,\"time\":\"2019-08-23T23:50:35.919Z\",\"statusCode\":200}";
    public static final String invalidCDCResponse = "{\"socialProviders\":\"site,oidc-fedspikegidp\",\"lastLogin\":\"2019-08-21T23:13:38.284Z\",\"userInfo\":{\"country\":\"United States\",\"isTempUser\":false,\"oldestDataAge\":-2147483648,\"capabilities\":\"None\",\"isSiteUID\":true,\"loginProviderUID\":\"ef632aa3f52140aa836673469378d0ac\",\"city\":\"ted\",\"isConnected\":true,\"errorCode\":0,\"isSiteUser\":true,\"loginProvider\":\"" + loginProvider + "\",\"oldestDataUpdatedTimestamp\":0,\"UID\":\"" + uid + "\",\"identities\":[{\"country\":\"United States\",\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"lastUpdatedTimestamp\":1566429217356,\"isExpiredSession\":false,\"allowsLogin\":false,\"city\":\"ted\",\"provider\":\"site\",\"isLoginIdentity\":false,\"oldestDataUpdated\":\"0001-01-01T00:00:00Z\",\"oldestDataUpdatedTimestamp\":0,\"providerUID\":\"ffb10070d8174a518f2e8b403c1efe5d\"},{\"lastUpdated\":\"2019-08-21T23:13:38.284Z\",\"lastUpdatedTimestamp\":1566429218284,\"isExpiredSession\":false,\"allowsLogin\":true,\"provider\":\"oidc-fedspikegidp\",\"isLoginIdentity\":true,\"nickname\":\"federatedUser\",\"oldestDataUpdated\":\"2019-08-21T23:01:23.988Z\",\"oidcData\":{},\"oldestDataUpdatedTimestamp\":1566428483988,\"email\":\"test@gmail.com\",\"providerUID\":\"ef632aa3f52140aa836673469378d0ac\"}],\"statusReason\":\"OK\",\"nickname\":\"federatedUser\",\"isLoggedIn\":true,\"time\":\"2019-08-23T23:50:35.918Z\",\"email\":\"test@gmail.com\",\"providers\":\"site,oidc-fedspikegidp\",\"statusCode\":200},\"isVerified\":true,\"errorCode\":0,\"registered\":\"2019-08-19T21:11:52.372Z\",\"isActive\":true,\"oldestDataUpdatedTimestamp\":1566248846440,\"emails\":{\"verified\":[\"test@gmail.com\"],\"unverified\":[]},\"lastUpdated\":\"2019-08-21T23:13:37.356Z\",\"apiVersion\":2,\"statusReason\":\"OK\",\"verifiedTimestamp\":1566248848104,\"oldestDataUpdated\":\"2019-08-19T21:07:26.440Z\",\"callId\":\"52317e98c0a849438f432669c5d198f0\",\"lastUpdatedTimestamp\":1566429217356,\"created\":\"2019-08-19T21:07:26.440Z\",\"createdTimestamp\":1566248846000,\"profile\":{\"locale\":\"" + localeName + "\",\"country\":\"United States\",\"city\":\"ted\",\"nickname\":\"siteUser\",\"email\":\"test@gmail.com\"},\"regSource\":\"http://dev2.apps.thermofisher.com/apps/fedspike/enterpriselogin\",\"verified\":\"2019-08-19T21:07:28.104Z\",\"registeredTimestamp\":1566249112000,\"loginProvider\":\"" + loginProvider + "\",\"lastLoginTimestamp\":1566429218000,\"UID\":\"ffb10070d8174a518f2e8b403c1efe5d\",\"isRegistered\":true,\"time\":\"2019-08-23T23:50:35.919Z\",\"statusCode\":200}";

    public static AccountInfo getFederatedAccount() {
        return AccountInfo.builder()
                .uid(uid)
                .username(federatedUsername)
                .emailAddress(federatedEmailAddress)
                .password("")
                .firstName(firstName)
                .lastName(lastName)
                .localeName(localeName)
                .company(company)
                .country(country)
                .city(city)
                .department(department)
                .member(member)
                .loginProvider(loginProvider)
                .regAttempts(0)
                .build();
    }

    public static AccountInfo getSiteAccount() {
        return AccountInfo.builder()
                .uid(uid)
                .username(username)
                .emailAddress(email)
                .password(algorithm + ":" + hash)
                .jobRole(jobRole)
                .interest(interest)
                .firstName(firstName)
                .phoneNumber(phoneNumber)
                .lastName(lastName)
                .localeName(localeName)
                .company(company)
                .country(country)
                .city(city)
                .department(department)
                .member(member)
                .loginProvider(loginProvider)
                .regAttempts(0)
                .hiraganaName(hiraganaName)
                .build();
    }

    public static AccountInfoDTO getAccountInfoDTO() {
        return AccountInfoDTO.builder()
        .username(username)
        .emailAddress(email)
        .password(password)
        .firstName(firstName)
        .lastName(lastName)
        .localeName(localeName)
        .company(company)
        .country(country)
        .city(city)
        .department(department)
        .member(member)
        .reCaptchaToken(reCaptchaToken)
        .build();
    }

    public static CDCNewAccount getNewCDCAccount(AccountInfo accountInfo) throws JSONException {
        Thermofisher thermofisher = Thermofisher.builder()
            .legacyUsername(accountInfo.getUsername())
            .build();

        China china = China.builder()
            .jobRole(jobRole)
            .interest(interest)
            .phoneNumber(phoneNumber)
            .build();

        Japan japan = Japan.builder()
            .hiraganaName(hiraganaName)
            .build();

        Registration registration = Registration.builder()
            .china(china)
            .japan(japan)
            .build();

        Data data = Data.builder()
            .subscribe(accountInfo.getMember())
            .thermofisher(thermofisher)
            .registration(registration)
            .build();

        Work work = Work.builder()
            .company(accountInfo.getCompany())
            .location(accountInfo.getDepartment())
            .build();

        Profile profile = Profile.builder()
            .firstName(accountInfo.getFirstName())
            .lastName(accountInfo.getLastName())
            .country(accountInfo.getCountry())
            .city(accountInfo.getCity())
            .locale(Utils.parseLocale(accountInfo.getLocaleName()))
            .work(work)
            .timezone(accountInfo.getTimezone())
            .build();

        CDCNewAccount newAccount = CDCNewAccount.builder()
            .username(accountInfo.getUsername())
            .email(accountInfo.getEmailAddress())
            .password(accountInfo.getPassword())
            .profile(profile)
            .data(data)
            .build();

        return newAccount;
    }

    public static Data getData() {
        return Data.builder()
            .thermofisher(getThermofisher())
            .subscribe("false")
            .build();
    }

    public static Profile getProfile() {
        return Profile.builder()
            .username(username)
            .email(email)
            .firstName(firstName)
            .lastName(lastName)
            .country(country)
            .city(city)
            .work(getWork())
            .timezone(timezone)
            .build();
    }

    public static Work getWork() {
        return Work.builder()
            .company(company)
            .location(location)
            .build();
    }

    public static Thermofisher getThermofisher() {
        return Thermofisher.builder()
            .legacyEmail(email)
            .legacyUsername(username)
            .build();
    }

    public static ObjectNode prepareJsonForNotification(ObjectNode json) {
        List<String> propertiesToRemove = new ArrayList<>();
        propertiesToRemove.add("member");
        propertiesToRemove.add("localeName");
        propertiesToRemove.add("loginProvider");
        propertiesToRemove.add("regAttempts");
        json.remove(propertiesToRemove);
        return json;
    }
}
