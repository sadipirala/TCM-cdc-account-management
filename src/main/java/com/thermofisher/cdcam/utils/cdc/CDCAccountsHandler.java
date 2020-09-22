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
            .build();

        setHiraganaName(accountInfo, data);

        Work work = Work.builder()
            .company(accountInfo.getCompany())
            .location(accountInfo.getDepartment())
            .build();

        Profile profile = Profile.builder()
            .firstName(accountInfo.getFirstName())
            .lastName(accountInfo.getLastName())
            .country(accountInfo.getCountry())
            .city(accountInfo.getCity())
            .locale(locale)
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

    private static void setHiraganaName(AccountInfo accountInfo, Data data) {
        if(hiraganaNameHasValue(accountInfo)) {
            Japan japan = Japan.builder()
                .hiraganaName(accountInfo.getHiraganaName())
                .build();

            Registration registration = Registration.builder()
                .japan(japan)
                .build();

            data.setRegistration(registration);
        }
    }

    private static boolean hiraganaNameHasValue(AccountInfo accountInfo) {
        return accountInfo.getHiraganaName() != null && !accountInfo.getHiraganaName().isEmpty();
    }
}
