package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EECUser {
    private String uid;
    private String email;
    private boolean registered;
    private int cdcResponseCode;
    private String cdcResponseMessage;
}
