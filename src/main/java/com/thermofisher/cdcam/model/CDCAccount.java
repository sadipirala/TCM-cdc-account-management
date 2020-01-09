package com.thermofisher.cdcam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CDCAccount {

    @JsonProperty("UID")
    private String UID;

    @JsonProperty("isRegistered")
    private Object isRegistered;

    private Profile profile;
    private Data data;
}