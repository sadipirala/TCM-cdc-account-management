package com.thermofisher.cdcam.model;

import com.thermofisher.cdcam.utils.EmailLocaleUtils;

import lombok.Getter;

@Getter
public class RegistrationConfirmation {
    private final String type = "ConfirmationEmail";
    private String locale;
    private EmailUserInfo userInfo;

    public RegistrationConfirmation build(AccountInfo account, String redirectUrl) {
        this.locale = EmailLocaleUtils.processLocaleForNotification(account.getLocaleName(), account.getCountry());
        this.userInfo = EmailUserInfo.builder()
                .email(account.getEmailAddress())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .redirectUrl(redirectUrl)
                .build();

        return this;
    }
}
