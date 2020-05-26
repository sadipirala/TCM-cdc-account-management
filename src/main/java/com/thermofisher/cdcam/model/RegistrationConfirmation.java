package com.thermofisher.cdcam.model;

import lombok.Getter;

@Getter
public class RegistrationConfirmation {

    private final String type = "ConfirmationEmail";
    private String locale;
    private EmailUserInfo userInfo;

    public RegistrationConfirmation build(AccountInfo account, String redirectUrl) {
        this.locale = account.getLocaleName();
        this.userInfo = EmailUserInfo.builder()
                .email(account.getEmailAddress())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .redirectUrl(redirectUrl)
                .build();

        return this;
    }
}
