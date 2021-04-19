package com.thermofisher.cdcam.model.cdc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonDeserialize(builder = Korea.KoreaBuilder.class)
public class Korea {
    private Boolean websiteTermsOfUse;
    private Boolean eCommerceTermsOfUse;
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
}
