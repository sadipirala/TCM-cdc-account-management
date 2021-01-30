package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountAvailabilityResponse {
    private Boolean isCDCAvailable;
}
