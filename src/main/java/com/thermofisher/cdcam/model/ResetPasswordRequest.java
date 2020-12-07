package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ResetPasswordRequest {
    private String username;
    private String captchaToken;
    private Boolean isReCaptchaV2;
}
