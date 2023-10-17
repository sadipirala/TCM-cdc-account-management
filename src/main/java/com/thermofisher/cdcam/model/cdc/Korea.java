package com.thermofisher.cdcam.model.cdc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Builder
@JsonDeserialize(builder = Korea.KoreaBuilder.class)
public class Korea {
    private Boolean receiveMarketingInformation;
    private Boolean thirdPartyTransferPersonalInfoMandatory;
    private Boolean thirdPartyTransferPersonalInfoOptional;
    private Boolean collectionAndUsePersonalInfoMandatory;
    private Boolean collectionAndUsePersonalInfoOptional;
    private Boolean collectionAndUsePersonalInfoMarketing;
    private Boolean overseasTransferPersonalInfoMandatory;
    private Boolean overseasTransferPersonalInfoOptional;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class KoreaBuilder {
    }

    @JsonIgnore
    public static Korea buildFromPreferences(Preferences preferences) {
        Boolean receiveMarketingInformation = false;
        Boolean thirdPartyTransferPersonalInfoMandatory = false;
        Boolean thirdPartyTransferPersonalInfoOptional = false;
        Boolean collectionAndUsePersonalInfoMandatory = false;
        Boolean collectionAndUsePersonalInfoOptional = false;
        Boolean collectionAndUsePersonalInfoMarketing = false;
        Boolean overseasTransferPersonalInfoMandatory = false;
        Boolean overseasTransferPersonalInfoOptional = false;

        if (Objects.nonNull(preferences) && Objects.nonNull(preferences.getKorea())) {
            receiveMarketingInformation = Objects.nonNull(preferences.getKorea().getReceiveMarketingInformation()) && preferences.getKorea().getReceiveMarketingInformation().isConsentGranted();
            thirdPartyTransferPersonalInfoMandatory = Objects.nonNull(preferences.getKorea().getThirdPartyTransferPersonalInfoMandatory()) && preferences.getKorea().getThirdPartyTransferPersonalInfoMandatory().isConsentGranted();
            thirdPartyTransferPersonalInfoOptional = Objects.nonNull(preferences.getKorea().getThirdPartyTransferPersonalInfoOptional()) && preferences.getKorea().getThirdPartyTransferPersonalInfoOptional().isConsentGranted();
            collectionAndUsePersonalInfoMandatory = Objects.nonNull(preferences.getKorea().getCollectionAndUsePersonalInfoMandatory()) && preferences.getKorea().getCollectionAndUsePersonalInfoMandatory().isConsentGranted();
            collectionAndUsePersonalInfoOptional = Objects.nonNull(preferences.getKorea().getCollectionAndUsePersonalInfoOptional()) && preferences.getKorea().getCollectionAndUsePersonalInfoOptional().isConsentGranted();
            collectionAndUsePersonalInfoMarketing = Objects.nonNull(preferences.getKorea().getCollectionAndUsePersonalInfoMarketing()) && preferences.getKorea().getCollectionAndUsePersonalInfoMarketing().isConsentGranted();
            overseasTransferPersonalInfoMandatory = Objects.nonNull(preferences.getKorea().getOverseasTransferPersonalInfoMandatory()) && preferences.getKorea().getOverseasTransferPersonalInfoMandatory().isConsentGranted();
            overseasTransferPersonalInfoOptional = Objects.nonNull(preferences.getKorea().getOverseasTransferPersonalInfoOptional()) && preferences.getKorea().getOverseasTransferPersonalInfoOptional().isConsentGranted();
        }

        return Korea.builder()
                .receiveMarketingInformation(receiveMarketingInformation)
                .thirdPartyTransferPersonalInfoMandatory(thirdPartyTransferPersonalInfoMandatory)
                .thirdPartyTransferPersonalInfoOptional(thirdPartyTransferPersonalInfoOptional)
                .collectionAndUsePersonalInfoMandatory(collectionAndUsePersonalInfoMandatory)
                .collectionAndUsePersonalInfoOptional(collectionAndUsePersonalInfoOptional)
                .collectionAndUsePersonalInfoMarketing(collectionAndUsePersonalInfoMarketing)
                .overseasTransferPersonalInfoMandatory(overseasTransferPersonalInfoMandatory)
                .overseasTransferPersonalInfoOptional(overseasTransferPersonalInfoOptional)
                .build();
    }
}
