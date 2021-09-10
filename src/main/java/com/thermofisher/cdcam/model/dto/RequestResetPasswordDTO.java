package com.thermofisher.cdcam.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RequestResetPasswordDTO {
    private String authData;
    private String passwordToken;
}
