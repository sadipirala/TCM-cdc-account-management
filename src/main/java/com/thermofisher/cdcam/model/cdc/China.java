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
@JsonDeserialize(builder = China.ChinaBuilder.class)
public class China {
    private String interest;
    private String jobRole;
    private String phoneNumber;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class ChinaBuilder {
    }
}
