package com.thermofisher.cdcam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Getter;

@Builder(builderClassName = "CDCProfileBuilder", toBuilder = true)
@Getter
@JsonDeserialize(builder = CDCProfile.CDCProfileBuilder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CDCProfile {
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CDCProfileBuilder {

    }
}
