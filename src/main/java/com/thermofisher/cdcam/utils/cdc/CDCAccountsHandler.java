package com.thermofisher.cdcam.utils.cdc;

import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.*;
import com.thermofisher.cdcam.utils.Utils;

import org.json.JSONException;

public class CDCAccountsHandler {

    public static CDCNewAccount buildCDCNewAccount(AccountInfo accountInfo) throws JSONException {
        String locale = (accountInfo.getLocaleName() == null) ? null : Utils.parseLocale(accountInfo.getLocaleName());

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

    private static Work buildWorkObject(AccountInfo accountInfo) {
        return accountInfo.getMember().equals("false") ? null : Work.builder()
                .company(accountInfo.getCompany())
                .location(accountInfo.getDepartment())
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

        if (accountInfo.getMember().equals("true")) {
            profile.setCity(accountInfo.getCity());
        }
        return profile;
    }


    private static Registration buildRegistrationObject(AccountInfo accountInfo) {
        Japan japan = Japan.builder()
                .hiraganaName(accountInfo.getHiraganaName())
                .build();

        China china = China.builder()
                .interest(accountInfo.getInterest())
                .jobRole(accountInfo.getJobRole())
                .phoneNumber(getPhoneNumberForChina(accountInfo))
                .build();

        Korea korea = Korea.builder()
                .eComerceTransaction(accountInfo.getECommerceTransaction())
                .personalInfoMandatory(accountInfo.getPersonalInfoMandatory())
                .personalInfoOptional(accountInfo.getPersonalInfoOptional())
                .privateInfoMandatory(accountInfo.getPrivateInfoMandatory())
                .privateInfoOptional(accountInfo.getPrivateInfoOptional())
                .processingConsignment(accountInfo.getProcessingConsignment())
                .termsOfUse(accountInfo.getTermsOfUse())
                .build();

        return Registration.builder()
                .japan(japan)
                .china(china)
                .korea(korea)
                .build();
    }

    private static String getPhoneNumberForChina(AccountInfo accountInfo) {
        return accountInfo.getMember().equals("true") && accountInfo.getCountry().toLowerCase().equals("cn") ? accountInfo.getPhoneNumber() : null;
    }
}
