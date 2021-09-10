package com.thermofisher.cdcam.model;

import com.thermofisher.cdcam.model.dto.RequestResetPasswordDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmailRequestResetPassword {
    private final String type = "RequestResetPassword";
    private String resetPasswordUrl;
    private String locale;
    private EmailUserInfo userInfo;

    public static EmailRequestResetPassword build(AccountInfo account, RequestResetPasswordDTO requestResetPasswordDTO, String resetPasswordUrl) {
        EmailUserInfo emailUserInfo = EmailUserInfo.builder()
                .email(account.getEmailAddress())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .username(account.getUsername())
                .redirectUrl(String.format("%s?pwrt=%s&id=%s&authData=%s", resetPasswordUrl, requestResetPasswordDTO.getPasswordToken(), account.getUid(), requestResetPasswordDTO.getAuthData()))
                .build();
        EmailRequestResetPassword emailRequestResetPassword = EmailRequestResetPassword.builder()
                .locale(account.getLocaleName())
                .userInfo(emailUserInfo)
                .build();

        return emailRequestResetPassword;
    }
}
