package com.thermofisher.cdcam.enums.cdc;

public enum Events {
    REGISTRATION("accountRegistered");

    private String value;

    Events(String value) { this.value = value; }

    public String getValue() { return this.value; }
}