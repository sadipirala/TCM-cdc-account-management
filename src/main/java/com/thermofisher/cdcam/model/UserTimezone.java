package com.thermofisher.cdcam.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserTimezone {
    @NotBlank
    private String uid;
    @NotBlank
    private String timezone;
}