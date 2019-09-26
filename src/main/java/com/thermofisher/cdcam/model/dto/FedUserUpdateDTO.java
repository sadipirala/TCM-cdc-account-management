package com.thermofisher.cdcam.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Getter;

/**
 * CDCUserUpdate
 */
@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FedUserUpdateDTO {
    private String uid;
    private String username;
    private Boolean regStatus;

    public boolean hasNullProperty() {
        return (uid == null || username == null || regStatus == null);
    }
}