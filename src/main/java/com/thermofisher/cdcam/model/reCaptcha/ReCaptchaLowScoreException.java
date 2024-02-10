package com.thermofisher.cdcam.model.reCaptcha;

/**
 * The class {@code ReCaptchaLowScoreException} is used to indicate
 * a low score error from reCaptcha V3 token validation response.
 */
public class ReCaptchaLowScoreException extends Exception {

    private static final long serialVersionUID = 1L;

    public ReCaptchaLowScoreException(String errorMessage) {
        super(errorMessage);
    }
}