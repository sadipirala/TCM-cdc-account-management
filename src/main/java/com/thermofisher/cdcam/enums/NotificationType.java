package com.thermofisher.cdcam.enums;

public enum NotificationType {
    MERGE("accountMerged"),
    REGISTRATION("accountRegistered");

    private String value;

    NotificationType(String value) { this.value = value; }

    public String getValue() { return this.value; }
}
