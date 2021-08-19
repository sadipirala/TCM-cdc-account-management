package com.thermofisher.cdcam.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChinaDTO {
    private String[] interest;
    private String[] jobRole;
    private String phoneNumber;
}
