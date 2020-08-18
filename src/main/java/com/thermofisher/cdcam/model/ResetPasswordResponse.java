package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ResetPasswordResponse {
    private String email;
    private String responseCode;
    private String responseMessage;
}
