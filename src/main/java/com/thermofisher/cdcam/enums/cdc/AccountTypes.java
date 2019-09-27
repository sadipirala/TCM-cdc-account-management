package com.thermofisher.cdcam.enums.cdc;

public enum AccountTypes {
    FULL_LITE("full,lite");

    private String value;

    AccountTypes(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
