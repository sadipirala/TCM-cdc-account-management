package com.thermofisher.cdcam.utils;

import java.io.IOException;

import com.thermofisher.cdcam.enums.CountryCodes;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;
import com.thermofisher.cdcam.model.cdc.China;
import com.thermofisher.cdcam.model.cdc.Data;
import com.thermofisher.cdcam.model.cdc.Japan;
import com.thermofisher.cdcam.model.cdc.Korea;
import com.thermofisher.cdcam.model.cdc.Profile;
import com.thermofisher.cdcam.model.cdc.Registration;
import com.thermofisher.cdcam.model.cdc.Thermofisher;
import com.thermofisher.cdcam.model.cdc.Work;
import com.thermofisher.cdcam.model.dto.AccountInfoDTO;
import org.json.JSONException;
import org.json.simple.parser.ParseException;
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
    public static final String federationSocialProviders = "saml-FID-TF-Centrify,site";
    public static final String company = "company";
    public static final String location = "Digital Engineering";
    public static final String timezone = "America/Tijuana";
    public static final String hiraganaName = "ひらがな";
    public static final String jobRole = "Development";
    public static final String interest = "Test interest";
    public static final String phoneNumber = "6648675309";
    
    // Korea
    public static final Boolean websiteTermsOfUse = true;
    public static final Boolean eCommerceTermsOfUse = true;
    public static final Boolean thirdPartyTransferPersonalInfoMandatory = true;
    public static final Boolean thirdPartyTransferPersonalInfoOptional = true;
    public static final Boolean collectionAndUsePersonalInfoMandatory = true;
    public static final Boolean collectionAndUsePersonalInfoOptional = true;
    public static final Boolean collectionAndUsePersonalInfoMarketing = true;
    public static final Boolean overseasTransferPersonalInfoMandatory = true;
    public static final Boolean overseasTransferPersonalInfoOptional = true;

    // Aspire
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
                .socialProviders(federationSocialProviders)
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
                .socialProviders("site")
                .regAttempts(0)
                .hiraganaName(hiraganaName)
                .websiteTermsOfUse(websiteTermsOfUse)
                .eCommerceTermsOfUse(eCommerceTermsOfUse)
                .thirdPartyTransferPersonalInfoMandatory(thirdPartyTransferPersonalInfoMandatory)
                .thirdPartyTransferPersonalInfoOptional(thirdPartyTransferPersonalInfoOptional)
                .collectionAndUsePersonalInfoMandatory(collectionAndUsePersonalInfoMandatory)
                .collectionAndUsePersonalInfoOptional(collectionAndUsePersonalInfoOptional)
                .collectionAndUsePersonalInfoMarketing(collectionAndUsePersonalInfoMarketing)
                .overseasTransferPersonalInfoMandatory(overseasTransferPersonalInfoMandatory)
                .overseasTransferPersonalInfoOptional(overseasTransferPersonalInfoOptional)
                .build();
    }

    public static AccountInfo getAspireAccount() {
        AccountInfo account = AccountUtils.getSiteAccount();
        account.setAcceptsAspireTermsAndConditions(true);
        account.setIsHealthcareProfessional(true);
        account.setIsGovernmentEmployee(true);
        account.setIsProhibitedFromAcceptingGifts(true);
        account.setAcceptsAspireEnrollmentConsent(true);
        return account;
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

        Data data = Data.builder()
            .subscribe(accountInfo.getMember())
            .thermofisher(thermofisher)
            .registration(buildRegistrationObject(accountInfo))
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

    private static Registration buildRegistrationObject(AccountInfo accountInfo) {
        Japan japan = null;
        China china = null;
        Korea korea = null;

        if (accountInfo.getCountry().toLowerCase().equals(CountryCodes.JAPAN.getValue())) {
            japan = Japan.builder()
                .hiraganaName(accountInfo.getHiraganaName())
                .build();
        }

        if (accountInfo.getCountry().toLowerCase().equals(CountryCodes.CHINA.getValue())) {
            china = China.builder()
                .interest(accountInfo.getInterest())
                .jobRole(accountInfo.getJobRole())
                .phoneNumber(getPhoneNumberForChina(accountInfo))
                .build();   
        }

        if (accountInfo.getCountry().toLowerCase().equals(CountryCodes.KOREA.getValue())) {
            korea = Korea.builder()
                .websiteTermsOfUse(accountInfo.getWebsiteTermsOfUse())
                .eCommerceTermsOfUse(accountInfo.getECommerceTermsOfUse())
                .collectionAndUsePersonalInfoMandatory(accountInfo.getCollectionAndUsePersonalInfoMandatory())
                .thirdPartyTransferPersonalInfoMandatory(accountInfo.getThirdPartyTransferPersonalInfoMandatory())
                .overseasTransferPersonalInfoMandatory(accountInfo.getOverseasTransferPersonalInfoMandatory())
                .thirdPartyTransferPersonalInfoOptional(accountInfo.getThirdPartyTransferPersonalInfoOptional())
                .collectionAndUsePersonalInfoOptional(accountInfo.getCollectionAndUsePersonalInfoOptional())
                .collectionAndUsePersonalInfoMarketing(accountInfo.getCollectionAndUsePersonalInfoMarketing())
                .overseasTransferPersonalInfoOptional(accountInfo.getOverseasTransferPersonalInfoOptional())
                .build();
        }

        if (japan == null && china == null && korea == null) {
            return null;
        }

        return Registration.builder()
            .japan(japan)
            .china(china)
            .korea(korea)
            .build();
    }

    public static String getSiteAccountJsonString() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/site-account.json";
        return TestUtils.getJSONFromFile(path).toString();
    }

    public static String getFederatedAccountJsonString() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/federated-account.json";
        return TestUtils.getJSONFromFile(path).toString();
    }

    public static String getInvalidAccountJsonString() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/invalid-account.json";
        return TestUtils.getJSONFromFile(path).toString();
    }
}
