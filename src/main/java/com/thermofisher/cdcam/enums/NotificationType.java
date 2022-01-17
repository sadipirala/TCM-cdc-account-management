package com.thermofisher.cdcam.enums;

public enum NotificationType {
    MERGE("accountMerged"),
    REGISTRATION("accountRegistered"),
    UPDATE("accountUpdated"),
    EMAIL_UPDATED("emailUpdated"),
    MARKETING_CONSENT_UPDATED("marketingConsentUpdated");

    private String value;

    NotificationType(String value) { this.value = value; }

    public String getValue() { return this.value; }
}
