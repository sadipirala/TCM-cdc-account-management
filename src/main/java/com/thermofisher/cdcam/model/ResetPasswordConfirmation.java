package com.thermofisher.cdcam.model;

import lombok.Getter;

@Getter
public class ResetPasswordConfirmation {
    private final String type = "ResetPasswordConfirmation";
    private String locale;
    private EmailUserInfo userInfo;

    public ResetPasswordConfirmation build(AccountInfo account, String redirectUrl) {
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
