package com.thermofisher.cdcam.model.cdc;

/**
 * The class {@code CustomGigyaErrorException} is used to indicate
 * any non-specific error from CDC that an
 * application might want to catch.
 */
public class CustomGigyaErrorException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public CustomGigyaErrorException(String errorMessage) {
        super(errorMessage);
    }
}