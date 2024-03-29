package com.thermofisher.cdcam.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UsernameRecoveryDTO {
    @NotBlank
    private String locale;
    @NotNull
    private UsernameRecoveryUserInfoDTO userInfo;
}
