package com.thermofisher.cdcam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Getter;

@Builder(builderClassName = "ProfileBuilder", toBuilder = true)
@Getter
@JsonDeserialize(builder = Profile.ProfileBuilder.class)
public class Profile {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String country;
    private String city;
    private String locale;
    private Work work;
    private String timezone;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class ProfileBuilder {

    }
}
