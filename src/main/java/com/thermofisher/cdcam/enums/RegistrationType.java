package com.thermofisher.cdcam.enums;

public enum RegistrationType {
    BASIC("basic");

    private String value;

    RegistrationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
