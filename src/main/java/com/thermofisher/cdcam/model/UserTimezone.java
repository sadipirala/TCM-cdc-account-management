package com.thermofisher.cdcam.model;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserTimezone {
    @NotNull
    private String uid;
    @NotNull
    private String timezone;
}