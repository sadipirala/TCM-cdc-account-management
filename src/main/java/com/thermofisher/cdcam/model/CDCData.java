package com.thermofisher.cdcam.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CDCData {
    @JsonProperty("thermofisher")
    private CDCThermofisher thermofisher;
}
