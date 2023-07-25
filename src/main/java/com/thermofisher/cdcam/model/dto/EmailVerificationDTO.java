package com.thermofisher.cdcam.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationDTO {
    private String uid;
    private String previousEmail;
}
