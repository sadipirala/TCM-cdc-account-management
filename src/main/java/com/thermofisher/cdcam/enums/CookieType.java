package com.thermofisher.cdcam.enums;


public enum CookieType {
    RESET_PASSWORD("reset_password");

    private String value;

    CookieType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
