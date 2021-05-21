package com.thermofisher.cdcam.model.notifications;

import com.thermofisher.cdcam.enums.NotificationType;
import com.thermofisher.cdcam.model.AccountInfo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AccountUpdatedNotification {
    @Builder.Default
    private String type = NotificationType.UPDATE.getValue();
    private String uid;
    private String company;
    private String city;
    private String country;
    private String member;

    public static AccountUpdatedNotification build(AccountInfo accountInfo) {
        return AccountUpdatedNotification.builder()
            .uid(accountInfo.getUid())
            .company(accountInfo.getCompany())
            .city(accountInfo.getCity())
            .country(accountInfo.getCountry())
            .member(accountInfo.getMember())
            .build();
    }
}