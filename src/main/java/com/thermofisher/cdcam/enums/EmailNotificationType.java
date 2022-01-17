package com.thermofisher.cdcam.enums;

public enum EmailNotificationType {
    CONFIRMATION_EMAIL("ConfirmationEmail"),
    RETRIEVE_USER_NAME_EMAIL("RetrieveUserName"),
    REQUEST_RESET_PASSWORD_EMAIL("RequestResetPassword"),
    RESET_PASSWORD_CONFIRMATION_EMAIL("ResetPasswordConfirmation");

    private String value;

    EmailNotificationType(String value) { this.value = value; }

    public String getValue() { return this.value; }
}
