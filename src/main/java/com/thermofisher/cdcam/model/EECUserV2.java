package com.thermofisher.cdcam.model;

import java.util.Objects;

import com.thermofisher.cdcam.enums.ResponseCode;
import com.thermofisher.cdcam.model.cdc.CDCAccount;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class EECUserV2 extends EECUser {
    private Boolean isRegistered;
    private Boolean isActive;
    private String dataCenter;
    
    public static EECUserV2 buildLiteRegisteredUser(String UID, String email) {
        return EECUserV2.builder()
            .uid(UID)
            .username(null)
            .email(email)
            .isRegistered(false)
            .isActive(false)
            .isAvailable(true)
            .responseCode(ResponseCode.SUCCESS.getValue())
            .responseMessage("OK")
            .build();
    }

    public static EECUserV2 buildFromExistingAccount(CDCAccount account) {
        String username = Objects.isNull(account.getProfile()) ? null : account.getProfile().getUsername();
        String email = Objects.isNull(account.getProfile()) ? null : account.getProfile().getEmail();
        boolean isActive = Objects.isNull(account.getIsActive()) ? false : account.getIsActive();
        boolean isRegistered = Objects.isNull(account.getIsRegistered()) ? false : account.getIsRegistered();

        return EECUserV2.builder()
            .uid(account.getUID())
            .username(username)
            .email(email)
            .isRegistered(isRegistered)
            .isActive(isActive)
            .isAvailable(false)
            .responseCode(ResponseCode.LOGINID_ALREADY_EXISTS.getValue())
            .responseMessage("Account already exists.")
            .build();
    }

    public static EECUserV2 buildInvalidUser(String email, int errorCode, String errorMessage) {
        return EECUserV2.builder()
            .email(email)
            .responseCode(errorCode)
            .responseMessage(errorMessage)
            .build();
    }
}
