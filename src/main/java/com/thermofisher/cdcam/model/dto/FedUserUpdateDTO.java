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
    private boolean regStatus;
}