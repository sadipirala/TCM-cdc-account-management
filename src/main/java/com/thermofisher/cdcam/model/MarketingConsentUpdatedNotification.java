package com.thermofisher.cdcam.model;

import com.thermofisher.cdcam.enums.NotificationType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class MarketingConsentUpdatedNotification {
    @Builder.Default
    private final String type = NotificationType.MARKETING_CONSENT_UPDATED.getValue();
    private String uid;
    private String city;
    private String country;
    private String company;
    private Boolean marketingConsent;

    public static MarketingConsentUpdatedNotification build(AccountInfo accountInfo) {
        return MarketingConsentUpdatedNotification.builder()
                .uid(accountInfo.getUid())
                .marketingConsent(accountInfo.isMarketingConsent())
                .city(accountInfo.getCity())
                .country(accountInfo.getCountry())
                .company(accountInfo.getCompany())
                .build();
    }
}
