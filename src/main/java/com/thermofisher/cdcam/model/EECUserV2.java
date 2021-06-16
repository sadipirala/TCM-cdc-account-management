package com.thermofisher.cdcam.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class EECUserV2 extends EECUser{
    private Boolean isRegistered;
    private Boolean isActive;
}
