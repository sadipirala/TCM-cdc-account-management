package com.thermofisher.cdcam.model.cdc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonDeserialize(builder = Korea.KoreaBuilder.class)
public class Korea {
    private Boolean eComerceTransaction;
    private Boolean personalInfoMandatory;
    private Boolean personalInfoOptional;
    private Boolean privateInfoMandatory;
    private Boolean privateInfoOptional;
    private Boolean processingConsignment;
    private Boolean termsOfUse;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class KoreaBuilder {
    }
}
