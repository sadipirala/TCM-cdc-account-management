package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Setter
@Getter
public class InvalidArgumentExceptionCustomResponse {
    private final String message = "One or more invalid arguments where provided.";
    private List<String> errors;
}
