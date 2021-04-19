package com.thermofisher.cdcam.model.notifications;

import com.thermofisher.cdcam.enums.NotificationType;
import com.thermofisher.cdcam.model.AccountInfo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class MergedAccountNotification {
    @Builder.Default
    private String type = NotificationType.MERGE.getValue();
    private String uid;
    private String password;
    private String company;
    private String department;
    private String city;
    private String country;
    private String member;

    public static MergedAccountNotification buildFrom(AccountInfo accountInfo) {
        return MergedAccountNotification.builder()
            .uid(accountInfo.getUid())
            .password(accountInfo.getPassword())
            .company(accountInfo.getCompany())
            .department(accountInfo.getDepartment())
            .city(accountInfo.getCity())
            .country(accountInfo.getCountry())
            .member(accountInfo.getMember())
            .build();
    }
}
