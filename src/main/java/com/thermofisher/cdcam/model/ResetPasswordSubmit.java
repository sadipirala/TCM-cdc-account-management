package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Builder
@Getter
@Setter
public class ResetPasswordSubmit {
    @NotBlank
    private String uid;
    @NotBlank
    private String newPassword;
    @NotBlank
    private String resetPasswordToken;
}
