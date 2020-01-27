package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProfileTimezone {
    private String timezone;
}