package com.thermofisher.cdcam.utils;

import java.io.IOException;
import java.util.Objects;

import com.thermofisher.cdcam.enums.CountryCodes;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.Ciphertext;
import com.thermofisher.cdcam.model.cdc.*;
import com.thermofisher.cdcam.model.dto.AccountInfoDTO;
import com.thermofisher.cdcam.services.EmailVerificationService;
import com.thermofisher.cdcam.services.LocaleNameService;

import org.apache.commons.lang3.StringUtils;
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
    public static final boolean marketingConsent = true;
    public static final String localeName = "en_US";
    public static final String loginProvider = "oidc";
    public static final String federationSocialProviders = "saml-FID-TF-Centrify,site";
    public static final String company = "company";
    public static final String location = "Digital Engineering";
    public static final String timezone = "America/Tijuana";
    public static final String hiraganaName = "hiranganaName";
    public static final String jobRole = "Development, HR";
    public static final String interest = "Health, Lab";
    public static final String phoneNumber = "6648675309";
    public static final String cipdc = "us";
    public static final String openIdProviderId = "";
    
    
    // Korea
    public static final Boolean receiveMarketingInformation = true;
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

    //China
    public static  final String[] jobRoles = { "Development", "HR" };
    public static  final String[] interests = { "Health", "Lab" };

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
                .marketingConsent(marketingConsent)
                .loginProvider(loginProvider)
                .socialProviders(federationSocialProviders)
                .regAttempts(0)
                .openIdProviderId(openIdProviderId)
                .build();
    }

    public static AccountInfo getSiteAccount() {
        return AccountInfo.builder()
                .uid(uid)
                .username(username)
                .emailAddress(email)
                .password(algorithm + ":" + hash)
                .firstName(firstName)
                .lastName(lastName)
                .localeName(localeName)
                .company(company)
                .country(country)
                .city(city)
                .marketingConsent(marketingConsent)
                .loginProvider(loginProvider)
                .socialProviders("site")
                .regAttempts(0)
                .openIdProviderId(openIdProviderId)
                .build();
    }

    public static AccountInfo getSiteAccountWithoutLocale() {
        return AccountInfo.builder()
                .uid(uid)
                .username(username)
                .emailAddress(email)
                .password(algorithm + ":" + hash)
                .firstName(firstName)
                .lastName(lastName)
                .company(company)
                .country(country)
                .city(city)
                .marketingConsent(marketingConsent)
                .loginProvider(loginProvider)
                .socialProviders("site")
                .regAttempts(0)
                .openIdProviderId(openIdProviderId)
                .build();
    }

    public static AccountInfo getSiteAccountChina() {
        return AccountInfo.builder()
                .uid(uid)
                .username(username)
                .emailAddress(email)
                .password(algorithm + ":" + hash)
                .phoneNumber(phoneNumber)
                .jobRole(jobRole)
                .interest(interest)
                .firstName(firstName)
                .lastName(lastName)
                .localeName(localeName)
                .company(company)
                .country(CountryCodes.CHINA.getValue())
                .city(city)
                .marketingConsent(marketingConsent)
                .loginProvider(loginProvider)
                .socialProviders("site")
                .regAttempts(0)
                .openIdProviderId("")
                .build();
    }

    public static AccountInfo getSiteAccountKorea() {
        return AccountInfo.builder()
                .uid(uid)
                .username(username)
                .emailAddress(email)
                .password(algorithm + ":" + hash)
                .firstName(firstName)
                .lastName(lastName)
                .localeName(localeName)
                .company(company)
                .country(CountryCodes.KOREA.getValue())
                .city(city)
                .marketingConsent(marketingConsent)
                .loginProvider(loginProvider)
                .socialProviders("site")
                .regAttempts(0)
                .receiveMarketingInformation(receiveMarketingInformation)
                .thirdPartyTransferPersonalInfoMandatory(thirdPartyTransferPersonalInfoMandatory)
                .thirdPartyTransferPersonalInfoOptional(thirdPartyTransferPersonalInfoOptional)
                .collectionAndUsePersonalInfoMandatory(collectionAndUsePersonalInfoMandatory)
                .collectionAndUsePersonalInfoOptional(collectionAndUsePersonalInfoOptional)
                .collectionAndUsePersonalInfoMarketing(collectionAndUsePersonalInfoMarketing)
                .overseasTransferPersonalInfoMandatory(overseasTransferPersonalInfoMandatory)
                .overseasTransferPersonalInfoOptional(overseasTransferPersonalInfoOptional)
                .openIdProviderId(openIdProviderId)
                .build();
    }

    public static AccountInfo getSiteAccountJapan() {
        return AccountInfo.builder()
                .uid(uid)
                .username(username)
                .emailAddress(email)
                .password(algorithm + ":" + hash)
                .firstName(firstName)
                .lastName(lastName)
                .localeName(localeName)
                .company(company)
                .country(CountryCodes.JAPAN.getValue())
                .city(city)
                .marketingConsent(marketingConsent)
                .loginProvider(loginProvider)
                .socialProviders("site")
                .regAttempts(0)
                .hiraganaName(hiraganaName)
                .openIdProviderId(openIdProviderId)
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
        .marketingConsent(marketingConsent)
        .reCaptchaToken(reCaptchaToken)
        .isReCaptchaV2(false)
        .acceptsAspireEnrollmentConsent(acceptsAspireEnrollmentConsent)
        .isHealthcareProfessional(isHealthcareProfessional)
        .isGovernmentEmployee(isGovernmentEmployee)
        .isProhibitedFromAcceptingGifts(isProhibitedFromAcceptingGifts)
        .acceptsAspireTermsAndConditions(acceptsAspireTermsAndConditions)
        .jobRoles(jobRoles)
        .interests(interests)
        .build();
    }

    public static CDCNewAccount getNewCDCAccount(AccountInfo accountInfo) throws JSONException {
        LocaleNameService localeNameService = new LocaleNameService();
        String locale = accountInfo.getLocaleName() == null ? null : localeNameService.getLocale(accountInfo.getLocaleName(), accountInfo.getCountry());

        Thermofisher thermofisher = Thermofisher.builder()
            .legacyUsername(accountInfo.getUsername())
            .build();

        Data data = Data.builder()
            .thermofisher(thermofisher)
            .registration(buildRegistrationObject(accountInfo))
            .subscribe(accountInfo.isMarketingConsent())
            .requirePasswordCheck(false)
            .verifiedEmailDate(EmailVerificationService.getDefaultVerifiedDate(accountInfo.getCountry()))
            .build();

        Work work = buildWorkObject(accountInfo);

        Profile profile = buildProfileObject(accountInfo, work, locale);

        return CDCNewAccount.build(
            accountInfo.getUsername(), 
            accountInfo.getEmailAddress(), 
            accountInfo.getPassword(),
            data,
            profile);
    }

    public static CDCNewAccountV2 getNewCDCAccountV2(AccountInfo accountInfo) throws JSONException {
        LocaleNameService localeNameService = new LocaleNameService();
        String locale = accountInfo.getLocaleName() == null ? null : localeNameService.getLocale(accountInfo.getLocaleName(), accountInfo.getCountry());

        Thermofisher thermofisher = Thermofisher.builder()
                .legacyUsername(accountInfo.getUsername())
                .build();

        Data data = Data.builder()
                .thermofisher(thermofisher)
                .registration(buildRegistrationObject(accountInfo))
                .requirePasswordCheck(false)
                .verifiedEmailDate(EmailVerificationService.getDefaultVerifiedDate(accountInfo.getCountry()))
                .build();

        Work work = buildWorkObject(accountInfo);

        Profile profile = buildProfileObject(accountInfo, work, locale);
        Preferences preferences = buildPreferencesObject(accountInfo);

        return CDCNewAccountV2.build(
                accountInfo.getUsername(),
                accountInfo.getEmailAddress(),
                accountInfo.getPassword(),
                data,
                profile,
                preferences);
    }

    public static CDCAccount getCDCAccount(AccountInfo accountInfo) throws JSONException {
        LocaleNameService localeNameService = new LocaleNameService();
        String locale = accountInfo.getLocaleName() == null ? null : localeNameService.getLocale(accountInfo.getLocaleName(), accountInfo.getCountry());

        Thermofisher thermofisher = Thermofisher.builder()
            .legacyUsername(accountInfo.getUsername())
            .build();

        Data data = Data.builder()
            .thermofisher(thermofisher)
            .registration(buildRegistrationObject(accountInfo))
            .subscribe(accountInfo.isMarketingConsent())
            .requirePasswordCheck(false)
            .build();

        Work work = buildWorkObject(accountInfo);

        Profile profile = buildProfileObject(accountInfo, work, locale);

        CDCAccount account = CDCAccount.builder()
            .UID(accountInfo.getUid())
            .profile(profile)
            .data(data)
            .build();

        return account;
    }

    public static Data getData() {
        return Data.builder()
            .thermofisher(getThermofisher())
            .subscribe(false)
            .requirePasswordCheck(false)
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
            .build();
    }

    public static Thermofisher getThermofisher() {
        return Thermofisher.builder()
            .legacyEmail(email)
            .legacyUsername(username)
            .build();
    }

    private static String getPhoneNumberForChina(AccountInfo accountInfo) {
        return accountInfo.isMarketingConsent() && accountInfo.getCountry().toLowerCase().equals("cn") ? accountInfo.getPhoneNumber() : null;
    }

    private static Preferences buildPreferencesObject(AccountInfo accountInfo) {
        Consent consent = Consent.builder().isConsentGranted(accountInfo.isMarketingConsent()).build();
        Marketing marketing = Marketing.builder().consent(consent).build();
        KoreaMarketingConsent korea = null;
        if (accountInfo.getCountry().toLowerCase().equals(CountryCodes.KOREA.getValue())) {
            korea = KoreaMarketingConsent.build(accountInfo);
        }

        if (Objects.nonNull(korea)) {
            return Preferences.builder().marketing(marketing).korea(korea).build();
        }

        return Preferences.builder().marketing(marketing).build();
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

        if (accountInfo.isMarketingConsent()) {
            profile.setCity(accountInfo.getCity());
        }
        return profile;
    }

    private static Work buildWorkObject(AccountInfo accountInfo) {
        return !accountInfo.isMarketingConsent() ? null : Work.builder()
                .company(accountInfo.getCompany())
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
                .receiveMarketingInformation(accountInfo.getReceiveMarketingInformation())
                .collectionAndUsePersonalInfoMandatory(accountInfo.getCollectionAndUsePersonalInfoMandatory())
                .thirdPartyTransferPersonalInfoMandatory(accountInfo.getThirdPartyTransferPersonalInfoMandatory())
                .overseasTransferPersonalInfoMandatory(accountInfo.getOverseasTransferPersonalInfoMandatory())
                .thirdPartyTransferPersonalInfoOptional(accountInfo.getThirdPartyTransferPersonalInfoOptional())
                .collectionAndUsePersonalInfoOptional(accountInfo.getCollectionAndUsePersonalInfoOptional())
                .collectionAndUsePersonalInfoMarketing(accountInfo.getCollectionAndUsePersonalInfoMarketing())
                .overseasTransferPersonalInfoOptional(accountInfo.getOverseasTransferPersonalInfoOptional())
                .build();
        }

        OpenIdProvider openIdProvider = OpenIdProvider.builder().build();
        if (StringUtils.isNotBlank(accountInfo.getOpenIdProviderId())) {
            openIdProvider.setClientID(accountInfo.getOpenIdProviderId());
        }

        return Registration.builder()
            .japan(japan)
            .china(china)
            .korea(korea)
            .openIdProvider(openIdProvider)
            .build();
    }

    public static Ciphertext getCiphertext() {
        return Ciphertext.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .source("IAC")
                .build();
    }

    public static CDCResponse getCdcResponse() {
        return CDCResponse.builder()
            .errorCode(0)
            .build();
    }

    public static String getSiteAccountJsonString() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/site-account.json";
        return TestUtils.getJSONFromFile(path).toString();
    }

    public static String getSiteAccountJsonStringV2() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/site-account-v2.json";
        return TestUtils.getJSONFromFile(path).toString();
    }

    public static String getSiteAccountWithoutPreferencesJsonString() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/site-account-without-preferences.json";
        return TestUtils.getJSONFromFile(path).toString();
    }

    public static String getSiteAccountWithMarketingConsentAsFalse() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/site-account-marketing-consent-false.json";
        return TestUtils.getJSONFromFile(path).toString();
    }

    public static String getSiteAccountWithMarketingConsentAsFalseV2() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/site-account-marketing-consent-false-v2.json";
        return TestUtils.getJSONFromFile(path).toString();
    }

    public static String getSiteAccountIncomplete() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/site-account-incomplete.json";
        return TestUtils.getJSONFromFile(path).toString();
    }

    public static String getSiteAccountJapanJsonString() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/site-account-japan.json";
        return TestUtils.getJSONFromFile(path).toString();
    }

    public static String getSiteAccountKoreaJsonString() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/site-account-korea.json";
        return TestUtils.getJSONFromFile(path).toString();
    }

    public static String getSiteAccountKoreaJsonStringV2() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/site-account-korea-v2.json";
        return TestUtils.getJSONFromFile(path).toString();
    }

    public static String getSiteAccountChinaJsonString() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/site-account-china.json";
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
