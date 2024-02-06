package com.thermofisher.cdcam.model.cdc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonDeserialize(builder = Data.DataBuilder.class)
public class Data {
    private Thermofisher thermofisher;
    private Registration registration;
    private String awsQuickSightRole;
    private boolean requirePasswordCheck;
    private boolean subscribe;
    private String verifiedEmailDate;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class DataBuilder {
    }
}
