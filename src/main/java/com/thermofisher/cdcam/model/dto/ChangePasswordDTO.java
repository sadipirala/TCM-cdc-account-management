package com.thermofisher.cdcam.model.dto;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDTO {
    @NotBlank
    private String newPassword;
    @NotBlank
    private String password;
}
