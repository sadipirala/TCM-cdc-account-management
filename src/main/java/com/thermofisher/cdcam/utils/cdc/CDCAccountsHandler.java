package com.thermofisher.cdcam.utils.cdc;

import com.thermofisher.cdcam.model.*;
import com.thermofisher.cdcam.utils.Utils;

import org.json.JSONException;

public class CDCAccountsHandler {

    public static CDCNewAccount buildCDCNewAccount(AccountInfo accountInfo) throws JSONException {
        String locale = (accountInfo.getLocaleName() == null) ? null : Utils.parseLocale(accountInfo.getLocaleName());

        Thermofisher thermofisher = Thermofisher.builder()
            .legacyUsername(accountInfo.getUsername())
            .legacyEmail(accountInfo.getEmailAddress())
            .build();

        Data data = Data.builder()
            .subscribe(accountInfo.getMember())
            .thermofisher(thermofisher)
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
}
