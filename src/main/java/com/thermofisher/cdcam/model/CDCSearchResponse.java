package com.thermofisher.cdcam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CDCSearchResponse {
    private int errorCode;
    private int statusCode;
    private String statusReason;
    private List<CDCResult> results;
    private int totalCount;
}
