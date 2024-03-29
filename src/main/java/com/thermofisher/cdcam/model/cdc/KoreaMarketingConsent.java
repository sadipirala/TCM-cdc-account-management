package com.thermofisher.cdcam.model.cdc;

import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.korea.CollectionAndUsePersonalInfoMandatory;
import com.thermofisher.cdcam.model.cdc.korea.CollectionAndUsePersonalInfoMarketing;
import com.thermofisher.cdcam.model.cdc.korea.CollectionAndUsePersonalInfoOptional;
import com.thermofisher.cdcam.model.cdc.korea.OverseasTransferPersonalInfoMandatory;
import com.thermofisher.cdcam.model.cdc.korea.OverseasTransferPersonalInfoOptional;
import com.thermofisher.cdcam.model.cdc.korea.ReceiveMarketingInformation;
import com.thermofisher.cdcam.model.cdc.korea.ThirdPartyTransferPersonalInfoMandatory;
import com.thermofisher.cdcam.model.cdc.korea.ThirdPartyTransferPersonalInfoOptional;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class KoreaMarketingConsent {
    private ReceiveMarketingInformation receiveMarketingInformation;
    private ThirdPartyTransferPersonalInfoMandatory thirdPartyTransferPersonalInfoMandatory;
    private ThirdPartyTransferPersonalInfoOptional thirdPartyTransferPersonalInfoOptional;
    private CollectionAndUsePersonalInfoMandatory collectionAndUsePersonalInfoMandatory;
    private CollectionAndUsePersonalInfoOptional collectionAndUsePersonalInfoOptional;
    private CollectionAndUsePersonalInfoMarketing collectionAndUsePersonalInfoMarketing;
    private OverseasTransferPersonalInfoMandatory overseasTransferPersonalInfoMandatory;
    private OverseasTransferPersonalInfoOptional overseasTransferPersonalInfoOptional;

    public static KoreaMarketingConsent build(AccountInfo accountInfo) {
        ReceiveMarketingInformation receiveMarketingInformation = ReceiveMarketingInformation.builder().isConsentGranted(accountInfo.getReceiveMarketingInformation()).build();
        ThirdPartyTransferPersonalInfoMandatory thirdPartyTransferPersonalInfoMandatory = ThirdPartyTransferPersonalInfoMandatory.builder().isConsentGranted(accountInfo.getThirdPartyTransferPersonalInfoMandatory()).build();
        ThirdPartyTransferPersonalInfoOptional thirdPartyTransferPersonalInfoOptional = ThirdPartyTransferPersonalInfoOptional.builder().isConsentGranted(accountInfo.getThirdPartyTransferPersonalInfoOptional()).build();
        CollectionAndUsePersonalInfoMandatory collectionAndUsePersonalInfoMandatory = CollectionAndUsePersonalInfoMandatory.builder().isConsentGranted(accountInfo.getCollectionAndUsePersonalInfoMandatory()).build();
        CollectionAndUsePersonalInfoOptional collectionAndUsePersonalInfoOptional = CollectionAndUsePersonalInfoOptional.builder().isConsentGranted(accountInfo.getCollectionAndUsePersonalInfoOptional()).build();
        CollectionAndUsePersonalInfoMarketing collectionAndUsePersonalInfoMarketing = CollectionAndUsePersonalInfoMarketing.builder().isConsentGranted(accountInfo.getCollectionAndUsePersonalInfoMarketing()).build();
        OverseasTransferPersonalInfoMandatory overseasTransferPersonalInfoMandatory = OverseasTransferPersonalInfoMandatory.builder().isConsentGranted(accountInfo.getOverseasTransferPersonalInfoMandatory()).build();
        OverseasTransferPersonalInfoOptional overseasTransferPersonalInfoOptional = OverseasTransferPersonalInfoOptional.builder().isConsentGranted(accountInfo.getOverseasTransferPersonalInfoOptional()).build();

        return KoreaMarketingConsent.builder()
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