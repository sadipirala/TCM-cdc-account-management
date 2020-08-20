package com.thermofisher.cdcam.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder(builderClassName = "EmailsBuilder", toBuilder = true)
@JsonDeserialize(builder = Emails.EmailsBuilder.class)
public class Emails {
    private List<String> verified;
    private List<String> unverified;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class EmailsBuilder {
    }
}
