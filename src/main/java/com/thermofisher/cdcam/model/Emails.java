package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Emails {
    private String[] verified;
    private String[] unverified;
}
