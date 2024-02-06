package com.thermofisher.cdcam.model.cdc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
