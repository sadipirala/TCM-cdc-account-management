package com.thermofisher.cdcam.enums.cdc;

public enum APIMethods {
    GET("accounts.getAccountInfo");

    private String value;

    APIMethods(String value) { this.value = value; }

    public String getValue() { return this.value; }
}