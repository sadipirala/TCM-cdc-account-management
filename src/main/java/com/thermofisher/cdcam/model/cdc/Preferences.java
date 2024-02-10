package com.thermofisher.cdcam.model.cdc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Preferences {
    private Marketing marketing;
    @JsonProperty("korea")
    private KoreaMarketingConsent korea;
}
