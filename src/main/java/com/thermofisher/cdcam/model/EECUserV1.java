package com.thermofisher.cdcam.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class EECUserV1 extends EECUser {
    private Boolean registered;

    public static EECUser build(EECUserV2 eecUserV2) {
        return EECUserV1.builder()
            .uid(eecUserV2.getUid())
            .username(eecUserV2.getUsername())
            .email(eecUserV2.getEmail())
            .responseCode(eecUserV2.getResponseCode())
            .responseMessage(eecUserV2.getResponseMessage())
            .registered(eecUserV2.getIsRegistered())
            .isAvailable(eecUserV2.getIsAvailable())
            .build();
    }
}
