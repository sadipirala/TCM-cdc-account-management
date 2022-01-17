package com.thermofisher.cdcam.model.cdc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.thermofisher.cdcam.model.Emails;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = CDCAccount.CDCAccountBuilder.class)
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class CDCAccountBuilder {
    }
}
