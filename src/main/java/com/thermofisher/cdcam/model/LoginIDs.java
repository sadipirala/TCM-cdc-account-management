package com.thermofisher.cdcam.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderClassName = "LoginIDsBuilder", toBuilder = true)
@JsonDeserialize(builder = LoginIDs.LoginIDsBuilder.class)
public class LoginIDs {

    private String username;
    private String[] emails;
    private String[] unverifiedEmails;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class LoginIDsBuilder {
    }
}
