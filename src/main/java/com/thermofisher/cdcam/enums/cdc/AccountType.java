package com.thermofisher.cdcam.enums.cdc;

public enum AccountType {
    FULL("full"),
    FULL_LITE("full,lite");

    private String value;

    AccountType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
