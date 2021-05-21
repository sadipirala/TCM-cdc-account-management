package com.thermofisher.cdcam.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class MarketingConsentDTO {
    private Boolean consent;
    private String city;
    private String country;
    private String company;
}
