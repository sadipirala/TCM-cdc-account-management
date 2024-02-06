package com.thermofisher.cdcam.model.reCaptcha;

/**
 * The class {@code ReCaptchaUnsuccessfulResponseException} is used to indicate
 * an unsuccessful response from reCaptcha token validation response.
 */
public class ReCaptchaUnsuccessfulResponseException extends Exception {

    private static final long serialVersionUID = 1L;

    public ReCaptchaUnsuccessfulResponseException(String errorMessage) {
        super(errorMessage);
    }
}