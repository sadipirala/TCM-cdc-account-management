package com.thermofisher.cdcam.enums.cdc;

public enum WebhookEvent {
    MERGE("accountMerged"),
    REGISTRATION("accountRegistered"),
    UPDATE("accountUpdated");

    private String value;

    WebhookEvent(String value) { this.value = value; }

    public String getValue() { return this.value; }
}