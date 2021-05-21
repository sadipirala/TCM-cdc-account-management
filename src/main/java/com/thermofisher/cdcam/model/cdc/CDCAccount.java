package com.thermofisher.cdcam.model.cdc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thermofisher.cdcam.model.Emails;
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
    private Boolean isRegistered;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("emails")
    private Emails emails;

    private LoginIDs loginIDs;
    private Profile profile;
    private Data data;
}
