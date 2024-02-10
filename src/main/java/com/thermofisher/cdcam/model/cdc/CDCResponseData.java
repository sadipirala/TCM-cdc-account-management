package com.thermofisher.cdcam.model.cdc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CDCResponseData {
    @JsonProperty("UID")
    private String UID;
    private String statusReason;
    private int statusCode;
    private int errorCode;
    private String errorDetails;
    private String regToken;
    private List<CDCValidationError> validationErrors;
}
