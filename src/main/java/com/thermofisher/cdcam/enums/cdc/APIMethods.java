package com.thermofisher.cdcam.enums.cdc;

public enum APIMethods {
    GET("accounts.getAccountInfo"),
    INITREG("accounts.initRegistration"),
    SEARCH("accounts.search"),
    SETINFO("accounts.setAccountInfo");

    private String value;

    APIMethods(String value) { this.value = value; }

    public String getValue() { return this.value; }
}