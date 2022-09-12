package com.thermofisher.cdcam.model.cdc;


import lombok.Getter;

/**
 * The class {@code CustomGigyaErrorException} is used to indicate
 * any non-specific error from CDC that an
 * application might want to catch.
 */

@Getter
public class CustomGigyaErrorException extends Exception {
    private static final long serialVersionUID = 1L;
    private int errorCode;
    private String callId;

    public CustomGigyaErrorException(String errorMessage) {
        super(errorMessage);
    }

    public CustomGigyaErrorException(String errorMessage, int errorCode) {
        super(errorMessage);
        this.errorCode = errorCode;
    }

    public CustomGigyaErrorException(String errorMessage, int errorCode, String callId) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.callId = callId;
    }
}