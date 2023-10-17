package com.thermofisher.cdcam.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
