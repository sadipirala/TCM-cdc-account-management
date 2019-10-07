package com.thermofisher.cdcam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CDCResult {

    @JsonProperty("UID")
    private String UID;

    @JsonProperty("isRegistered")
    private Object isRegistered;

    private CDCProfile profile;
}
