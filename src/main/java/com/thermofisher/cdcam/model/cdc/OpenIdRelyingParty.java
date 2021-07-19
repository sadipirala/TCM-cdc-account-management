package com.thermofisher.cdcam.model.cdc;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenIdRelyingParty {

    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("redirectUris")
    private List<String> redirectUris;
}
