package com.thermofisher.cdcam.enums;

public enum CountryCodes {
    KOREA("kr"),
    CHINA("cn"),
    JAPAN("jp");

    private String value;

    CountryCodes(String value) { this.value = value; }

    public String getValue() { return this.value; }
}
