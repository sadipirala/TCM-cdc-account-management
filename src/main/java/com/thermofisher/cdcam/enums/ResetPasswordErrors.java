package com.thermofisher.cdcam.enums;

public enum ResetPasswordErrors {
    VERIFY_TOKEN_EXCEPTION("verify-token-exception"),
    CDC_EMAIL_NOT_FOUND("email-not-found"),
    SNS_NOT_SEND("error sending the reset password sns"),
    ACCOUNT_NOT_FOUND("no account found with username");

    private String value;

    ResetPasswordErrors(String value) { this.value = value; }

    public String getValue() { return this.value; }
}
