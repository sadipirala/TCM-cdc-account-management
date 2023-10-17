package com.thermofisher.cdcam.utils.cdc;

import com.thermofisher.cdcam.enums.CountryCodes;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.CDCNewAccount;
import com.thermofisher.cdcam.model.cdc.CDCNewAccountV2;
import com.thermofisher.cdcam.model.cdc.China;
import com.thermofisher.cdcam.model.cdc.Consent;
import com.thermofisher.cdcam.model.cdc.Data;
import com.thermofisher.cdcam.model.cdc.Japan;
import com.thermofisher.cdcam.model.cdc.Korea;
import com.thermofisher.cdcam.model.cdc.KoreaMarketingConsent;
import com.thermofisher.cdcam.model.cdc.Marketing;
import com.thermofisher.cdcam.model.cdc.OpenIdProvider;
import com.thermofisher.cdcam.model.cdc.Preferences;
import com.thermofisher.cdcam.model.cdc.Profile;
import com.thermofisher.cdcam.model.cdc.Registration;
import com.thermofisher.cdcam.model.cdc.Thermofisher;
import com.thermofisher.cdcam.model.cdc.Work;
import com.thermofisher.cdcam.services.EmailVerificationService;
import com.thermofisher.cdcam.services.LocaleNameService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import java.util.Objects;

public class CDCAccountsHandler {

    public static CDCNewAccount buildCDCNewAccount(AccountInfo accountInfo) throws JSONException {
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

    public static CDCNewAccountV2 buildCDCNewAccountV2(AccountInfo accountInfo) throws JSONException {
        LocaleNameService localeNameService = new LocaleNameService();
        String locale = accountInfo.getLocaleName() == null ? null : localeNameService.getLocale(accountInfo.getLocaleName(), accountInfo.getCountry());

        Thermofisher thermofisher = Thermofisher.builder()
                .legacyUsername(accountInfo.getUsername())
                .build();

        Data data = Data.builder()
                .thermofisher(thermofisher)
                .registration(buildRegistrationObjectV2(accountInfo))
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

    private static Work buildWorkObject(AccountInfo accountInfo) {
        return !accountInfo.isMarketingConsent() ? null : Work.builder()
                .company(accountInfo.getCompany())
                .build();
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

    private static Registration buildRegistrationObjectV2(AccountInfo accountInfo) {
        Japan japan = null;
        China china = null;

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

        OpenIdProvider openIdProvider = OpenIdProvider.builder().build();
        if (StringUtils.isNotBlank(accountInfo.getOpenIdProviderId())) {
            openIdProvider.setClientID(accountInfo.getOpenIdProviderId());
        }

        return Registration.builder()
                .japan(japan)
                .china(china)
                .openIdProvider(openIdProvider)
                .build();
    }

    private static String getPhoneNumberForChina(AccountInfo accountInfo) {
        return accountInfo.isMarketingConsent() && accountInfo.getCountry().toLowerCase().equals(CountryCodes.CHINA.getValue()) ? accountInfo.getPhoneNumber() : null;
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
}
