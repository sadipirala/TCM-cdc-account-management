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

    public String getHiraganaName () {
        return japan != null ? japan.getHiraganaName() : null;
    }

    public String getJobRole () {
        return china != null ? china.getJobRole() : null;
    }

    public String getInterest () {
        return china != null ? china.getInterest() : null;
    }

    public String getPhoneNumber () {
        return china != null ? china.getPhoneNumber() : null;
    }

    public Boolean getEcomerceTransaction () {
        return korea != null ? korea.getEComerceTransaction() : null;
    }

    public Boolean getPersonalInfoMandatory () {
        return korea != null ? korea.getPersonalInfoMandatory() : null;
    }

    public Boolean getPersonalInfoOptional () {
        return korea != null ? korea.getPersonalInfoOptional() : null;
    }

    public Boolean getPrivateInfoMandatory () {
        return korea != null ? korea.getPrivateInfoMandatory() : null;
    }

    public Boolean getPrivateInfoOptional () {
        return korea != null ? korea.getPrivateInfoOptional() : null;
    }

    public Boolean getProcessingConsignment () {
        return korea != null ? korea.getProcessingConsignment() : null;
    }

    public Boolean getTermsOfUse () {
        return korea != null ? korea.getTermsOfUse() : null;
    }
}
