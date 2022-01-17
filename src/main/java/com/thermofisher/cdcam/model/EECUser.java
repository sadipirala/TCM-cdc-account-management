package com.thermofisher.cdcam.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class EECUser {
    private String uid;
    private String username;
    private String email;
    private int responseCode;
    private String responseMessage;
    private Boolean isAvailable;
}
