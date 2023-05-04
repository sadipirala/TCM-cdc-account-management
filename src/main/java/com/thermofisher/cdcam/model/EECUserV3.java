package com.thermofisher.cdcam.model;

import java.text.MessageFormat;
import java.util.Objects;

import com.thermofisher.cdcam.enums.ResponseCode;
import com.thermofisher.cdcam.model.cdc.CDCAccount;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class EECUserV3 extends EECUser {
    private String passwordSetupLink;

    public static EECUserV3 buildLiteRegisteredUser(String uid, String email, String clientId, String redirectUri) {
        String generatedPasswordSetupLink = MessageFormat.format(redirectUri, clientId, uid);
        
        return EECUserV3.builder()
            .email(email)
            .isAvailable(true)
            .username(null)
            .passwordSetupLink(generatedPasswordSetupLink)
            .responseCode(200)
            .responseMessage("OK")
            .uid(uid)
            .build();
    }
    
    public static EECUserV3 buildFromExistingAccount(CDCAccount account) {
        String email = Objects.isNull(account.getProfile()) ? null : account.getProfile().getEmail();
        String username = Objects.isNull(account.getProfile()) ? null : account.getProfile().getUsername();
        
        return EECUserV3.builder()
            .email(email)
            .isAvailable(false)
            .passwordSetupLink("")
            .responseCode(ResponseCode.LOGINID_ALREADY_EXISTS.getValue())
            .responseMessage("Account already exists.")
            .uid(account.getUID())
            .username(username)
            .build();
    }

    public static EECUserV3 buildInvalidUser(String email, int errorCode, String errorMessage) {
        return EECUserV3.builder()
            .email(email)
            .responseCode(errorCode)
            .responseMessage(errorMessage)
            .build();
    }
}
