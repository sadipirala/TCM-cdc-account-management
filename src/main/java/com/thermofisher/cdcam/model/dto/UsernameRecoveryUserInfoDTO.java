package com.thermofisher.cdcam.model.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsernameRecoveryUserInfoDTO {
    private String email;
    private String redirectUrl;
}