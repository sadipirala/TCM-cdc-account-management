package com.thermofisher.cdcam.model.notifications;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PasswordUpdateNotification {
    private String uid;
    private String newPassword;
}
