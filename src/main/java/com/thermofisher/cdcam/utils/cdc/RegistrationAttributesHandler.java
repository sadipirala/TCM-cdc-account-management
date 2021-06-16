package com.thermofisher.cdcam.utils.cdc;

import com.thermofisher.cdcam.model.cdc.China;
import com.thermofisher.cdcam.model.cdc.Japan;
import com.thermofisher.cdcam.model.cdc.Korea;
import com.thermofisher.cdcam.model.cdc.Registration;

public class RegistrationAttributesHandler {
    private final China china;
    private final Japan japan;
    private final Korea korea;

    public RegistrationAttributesHandler(Registration registration) {
        china = registration != null ? registration.getChina() : null;
        japan = registration != null ? registration.getJapan() : null;
        korea = registration != null ? registration.getKorea() : null;
    }

    public String getHiraganaName() {
        return japan != null ? japan.getHiraganaName() : null;
    }

    public String getJobRole() {
        return china != null ? china.getJobRole() : null;
    }

    public String getInterest() {
        return china != null ? china.getInterest() : null;
    }

    public String getPhoneNumber() {
        return china != null ? china.getPhoneNumber() : null;
    }

    // Korea

    public Boolean getWebsiteTermsOfUse() {
        return korea != null ? korea.getWebsiteTermsOfUse() : null;
    }

    public Boolean getECommerceTermsOfUse() {
        return korea != null ? korea.getECommerceTermsOfUse() : null;
    }

    public Boolean getThirdPartyTransferPersonalInfoMandatory() {
        return korea != null ? korea.getThirdPartyTransferPersonalInfoMandatory() : null;
    }

    public Boolean getThirdPartyTransferPersonalInfoOptional() {
        return korea != null ? korea.getThirdPartyTransferPersonalInfoOptional() : null;
    }

    public Boolean getCollectionAndUsePersonalInfoMandatory() {
        return korea != null ? korea.getCollectionAndUsePersonalInfoMandatory() : null;
    }

    public Boolean getCollectionAndUsePersonalInfoOptional() {
        return korea != null ? korea.getCollectionAndUsePersonalInfoOptional() : null;
    }

    public Boolean getCollectionAndUsePersonalInfoMarketing() {
        return korea != null ? korea.getCollectionAndUsePersonalInfoMarketing() : null;
    }

    public Boolean getOverseasTransferPersonalInfoMandatory() {
        return korea != null ? korea.getOverseasTransferPersonalInfoMandatory() : null;
    }

    public Boolean getOverseasTransferPersonalInfoOptional() {
        return korea != null ? korea.getOverseasTransferPersonalInfoOptional() : null;
    }
}
