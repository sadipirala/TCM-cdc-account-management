package com.thermofisher.cdcam.enums;

public enum CaptchaErrors {
    VERIFY_TOKEN_EXCEPTION("verify-token-exception"),
    CDC_EMAIL_NOT_FOUND("email-not-found");

    private String value;

    CaptchaErrors(String value) { this.value = value; }

    public String getValue() { return this.value; }
}
