package com.thermofisher.cdcam.enums.cdc;

/**
 * {@code enum} that holds response and error codes from CDC.
 * <p>
 * Reference https://developers.gigya.com/display/GD/Response+Codes+and+Errors
 */
public enum GigyaCodes {
    SUCCESS(0),
    ACCOUNT_PENDING_REGISTRATION(206001),
    PENDING_CODE_VERIFICATION(206006),
    LOGIN_ID_DOES_NOT_EXIST(403047),
    UID_NOT_FOUND(403005);

    private int value;

    GigyaCodes(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
