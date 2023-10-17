package com.thermofisher.cdcam.model.dto;

import com.thermofisher.cdcam.model.cdc.China;
import com.thermofisher.cdcam.model.cdc.Japan;
import com.thermofisher.cdcam.model.cdc.Korea;
import com.thermofisher.cdcam.model.cdc.OpenIdProvider;
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
    private OpenIdProvider openIdProvider;
}
