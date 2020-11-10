package com.thermofisher.cdcam.enums.cdc;

public enum APIMethods {
    GET("accounts.getAccountInfo"),
    INITREG("accounts.initRegistration"),
    IS_AVAILABLE_LOGINID("accounts.isAvailableLoginID"),
    SEARCH("accounts.search"),
    SETINFO("accounts.setAccountInfo"),
    REGISTER("accounts.register"),
    SEND_VERIFICATION_EMAIL("accounts.resendVerificationCode"),
    RESET_PASSWORD("accounts.resetPassword");

    private String value;

    APIMethods(String value) { this.value = value; }

    public String getValue() { return this.value; }
}
