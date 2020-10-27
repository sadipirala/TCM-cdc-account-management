package com.thermofisher.cdcam.model;

public class CustomGigyaErrorException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CustomGigyaErrorException(String errorMessage) {
        super(errorMessage);
    }
}