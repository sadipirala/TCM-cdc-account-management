package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CDCAccount {
    private CDCData data;
    private CDCProfile profile;
}
