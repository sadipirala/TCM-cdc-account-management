package com.thermofisher.cdcam.model;

import com.thermofisher.cdcam.enums.EmailNotificationType;
import com.thermofisher.cdcam.model.dto.RequestResetPasswordDTO;
import com.thermofisher.cdcam.model.dto.UsernameRecoveryDTO;
import com.thermofisher.cdcam.utils.EmailLocaleUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmailNotification {
    private String type;
    private String locale;
    private EmailUserInfo userInfo;
    private String clientId;

    public static EmailNotification buildConfirmationEmail(AccountInfo account) {
        EmailUserInfo emailUserInfo = EmailUserInfo.builder()
                .email(account.getEmailAddress())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .username(account.getUsername())
                .build();
        EmailNotification emailNotification = EmailNotification.builder()
                .locale(EmailLocaleUtils.processLocaleForNotification(account.getLocaleName(), account.getCountry()))
                .userInfo(emailUserInfo)
                .type(EmailNotificationType.CONFIRMATION_EMAIL.getValue())
                .clientId(account.getOpenIdProviderId())
                .build();

        return emailNotification;
    }

    public static EmailNotification buildResetPasswordNotification(AccountInfo account, String redirectUrl) {
        EmailUserInfo emailUserInfo = EmailUserInfo.builder()
                .email(account.getEmailAddress())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .username(account.getUsername())
                .redirectUrl(redirectUrl)
                .build();
        EmailNotification emailNotification = EmailNotification.builder()
                .locale(account.getLocaleName())
                .userInfo(emailUserInfo)
                .type(EmailNotificationType.RESET_PASSWORD_CONFIRMATION_EMAIL.getValue())
                .build();

        return emailNotification;
    }

    public static EmailNotification buildRetrieveUsernameNotification(UsernameRecoveryDTO usernameRecoveryDTO, AccountInfo account) {
        EmailUserInfo emailUserInfo = EmailUserInfo.builder()
                .email(usernameRecoveryDTO.getUserInfo().getEmail())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .username(account.getUsername())
                .redirectUrl(usernameRecoveryDTO.getUserInfo().getRedirectUrl())
                .build();
        EmailNotification emailNotification = EmailNotification.builder()
                .locale(EmailLocaleUtils.processLocaleForNotification(account.getLocaleName(), account.getCountry()))
                .userInfo(emailUserInfo)
                .type(EmailNotificationType.RETRIEVE_USER_NAME_EMAIL.getValue())
                .build();

        return emailNotification;
    }

    public static EmailNotification buildRequestResetPasswordNotification(AccountInfo account, RequestResetPasswordDTO requestResetPasswordDTO, String resetPasswordUrl) {
        EmailUserInfo emailUserInfo = EmailUserInfo.builder()
                .email(account.getEmailAddress())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .username(account.getUsername())
                .redirectUrl(buildResetPasswordUrl(requestResetPasswordDTO, account, resetPasswordUrl))
                .build();
        EmailNotification emailNotification = EmailNotification.builder()
                .locale(account.getLocaleName())
                .userInfo(emailUserInfo)
                .type(EmailNotificationType.REQUEST_RESET_PASSWORD_EMAIL.getValue())
                .build();

        return emailNotification;
    }

    private static String buildResetPasswordUrl(RequestResetPasswordDTO requestResetPasswordDTO, AccountInfo account, String resetPasswordUrl) {
        return String.format("%s?pwrt=%s&id=%s&authData=%s", resetPasswordUrl, requestResetPasswordDTO.getPasswordToken(), account.getUid(), requestResetPasswordDTO.getAuthData());
    }
}
