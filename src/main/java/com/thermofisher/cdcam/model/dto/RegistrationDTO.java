package com.thermofisher.cdcam.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thermofisher.cdcam.model.cdc.China;
import com.thermofisher.cdcam.model.cdc.Japan;
import com.thermofisher.cdcam.model.cdc.Korea;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegistrationDTO {
    private Japan japan;
    private China china;
    private Korea korea;
}
