package com.thermofisher.cdcam.enums.cdc;
/**
 * {@code enum} that holds response and error codes from CDC.
 * <p>
 * Reference https://developers.gigya.com/display/GD/Response+Codes+and+Errors
 */
public enum GigyaCodes {
    SUCCESS(0),
    LOGIN_ID_DOES_NOT_EXIST(403047);

    private int value;

    GigyaCodes(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
