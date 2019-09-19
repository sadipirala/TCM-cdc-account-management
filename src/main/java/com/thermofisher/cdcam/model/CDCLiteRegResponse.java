package com.thermofisher.cdcam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CDCLiteRegResponse {
    private int errorCode;
    private String errorMessage;
    private String errorDetails;
    private CDCData data;
}
