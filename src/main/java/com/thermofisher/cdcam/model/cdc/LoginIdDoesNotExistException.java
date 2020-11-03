package com.thermofisher.cdcam.model.cdc;

/**
 * The class {@code LoginIdDoesNotExistException} is used to indicate
 * that a LoginID has not been found in CDC.
 * <p>
 * Error code related to this exception is 403047.
 */
public class LoginIdDoesNotExistException extends Exception {

    private static final long serialVersionUID = 1L;

    public LoginIdDoesNotExistException(String message) {
        super(message);
    }
}
