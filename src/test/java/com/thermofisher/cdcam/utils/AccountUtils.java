package com.thermofisher.cdcam.utils;

import java.io.FileReader;
import java.io.IOException;

import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.*;
import com.thermofisher.cdcam.model.dto.AccountInfoDTO;

import org.json.JSONException;

import lombok.Getter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
    public static final Boolean eComerceTransaction = true;
    public static final Boolean personalInfoMandatory = true;
    public static final Boolean personalInfoOptional = true;
    public static final Boolean privateInfoMandatory = true;
    public static final Boolean privateInfoOptional = true;
    public static final Boolean processingConsignment = true;
    public static final Boolean termsOfUse = true;
    public static final Boolean acceptsAspireEnrollmentConsent = true;
    public static final Boolean isHealthcareProfessional = true;
    public static final Boolean isGovernmentEmployee = true;
    public static final Boolean isProhibitedFromAcceptingGifts = true;
    public static final Boolean acceptsAspireTermsAndConditions = true;

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
                .eCommerceTransaction(eComerceTransaction)
                .personalInfoMandatory(personalInfoMandatory)
                .personalInfoOptional(personalInfoOptional)
                .privateInfoOptional(privateInfoOptional)
                .privateInfoMandatory(privateInfoMandatory)
                .processingConsignment(processingConsignment)
                .termsOfUse(termsOfUse)
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
        .isReCaptchaV2(false)
        .acceptsAspireEnrollmentConsent(acceptsAspireEnrollmentConsent)
        .isHealthcareProfessional(isHealthcareProfessional)
        .isGovernmentEmployee(isGovernmentEmployee)
        .isProhibitedFromAcceptingGifts(isProhibitedFromAcceptingGifts)
        .acceptsAspireTermsAndConditions(acceptsAspireTermsAndConditions)
        .build();
    }

    public static CDCNewAccount getNewCDCAccount(AccountInfo accountInfo) throws JSONException {
        String locale = accountInfo.getLocaleName() != null ? Utils.parseLocale(accountInfo.getLocaleName()) : null;
        Thermofisher thermofisher = Thermofisher.builder()
            .legacyUsername(accountInfo.getUsername())
            .build();

        China china = China.builder()
            .jobRole(jobRole)
            .interest(interest)
            .phoneNumber(getPhoneNumberForChina(accountInfo))
            .build();

        Japan japan = Japan.builder()
            .hiraganaName(hiraganaName)
            .build();

        Korea korea = Korea.builder()
             .eComerceTransaction(eComerceTransaction)
             .personalInfoMandatory(personalInfoMandatory)
             .personalInfoOptional(personalInfoOptional)
             .privateInfoMandatory(privateInfoMandatory)
             .privateInfoOptional(privateInfoOptional)
             .processingConsignment(processingConsignment)
             .termsOfUse(termsOfUse)
             .build();

        Registration registration = Registration.builder()
            .china(china)
            .japan(japan)
            .korea(korea)
            .build();

        Data data = Data.builder()
            .subscribe(accountInfo.getMember())
            .thermofisher(thermofisher)
            .registration(registration)
            .build();

        Work work = buildWorkObject(accountInfo);

        Profile profile = buildProfileObject(accountInfo, work, locale);

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

    private static String getPhoneNumberForChina(AccountInfo accountInfo) {
        return accountInfo.getMember().equals("true") && accountInfo.getCountry().toLowerCase().equals("cn") ? accountInfo.getPhoneNumber() : null;
    }

    private static Profile buildProfileObject(AccountInfo accountInfo, Work work, String locale) {
        Profile profile = Profile.builder()
                .firstName(accountInfo.getFirstName())
                .lastName(accountInfo.getLastName())
                .locale(locale)
                .country(accountInfo.getCountry())
                .work(work)
                .timezone(accountInfo.getTimezone())
                .build();

        if (accountInfo.getMember().equals("true")) {
            profile.setCity(accountInfo.getCity());
        }
        return profile;
    }

    private static Work buildWorkObject(AccountInfo accountInfo) {
        return accountInfo.getMember().equals("false") ? null : Work.builder()
                .company(accountInfo.getCompany())
                .location(accountInfo.getDepartment())
                .build();
    }

    public static String getSiteAccountJsonString() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/site-account.json";
        return getJSONFromFile(path).toString();
    }

    public static String getFederatedAccountJsonString() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/federated-account.json";
        return getJSONFromFile(path).toString();
    }

    public static String getInvalidAccountJsonString() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/invalid-account.json";
        return getJSONFromFile(path).toString();
    }

    private static JSONObject getJSONFromFile (String filePath) throws IOException, ParseException{
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(filePath));
        return (JSONObject) obj;
    }
}
