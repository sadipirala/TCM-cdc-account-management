package com.thermofisher.cdcam.model.cdc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CDCResponseData {

    @JsonProperty("UID")
    private String UID;

    private String statusReason;

    private int statusCode;

    private String errorDetails;

    private List<CDCValidationError> validationErrors;
}
