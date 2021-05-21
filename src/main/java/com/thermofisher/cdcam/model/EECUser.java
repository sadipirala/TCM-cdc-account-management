package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EECUser {
    private String uid;
    private String username;
    private String email;
    private Boolean registered;
    // private Boolean isActive;
    private int responseCode;
    private String responseMessage;
}
