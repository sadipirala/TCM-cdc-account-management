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
@JsonDeserialize(builder = OpenIdProvider.OpenIdProviderBuilder.class)
public class OpenIdProvider {
    private String clientID;
    private String providerName;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class OpenIdProviderBuilder {
    }
}
