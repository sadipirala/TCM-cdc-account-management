package com.thermofisher.cdcam.model;

import com.thermofisher.cdcam.enums.NotificationType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class EmailUpdatedNotification {
    @Builder.Default
    private String type = NotificationType.EMAIL_UPDATED.getValue();
    private String uid;
    private String emailAddress;
    private String username;

    public static EmailUpdatedNotification build(AccountInfo accountInfo) {
        return EmailUpdatedNotification.builder()
                .uid(accountInfo.getUid())
                .emailAddress(accountInfo.getEmailAddress())
                .username(accountInfo.getUsername())
                .build();
    }
}
