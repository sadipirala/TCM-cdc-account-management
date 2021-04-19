package com.thermofisher.cdcam.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Builder
@Getter
@Setter
public class AccountInfoDTO {
    @NotBlank
    private String username;
    @NotBlank
    @Size(max = 30)
    private String firstName;
    @NotBlank
    @Size(max = 30)
    private String lastName;
    @NotBlank
    @Size(max = 50)
    private String emailAddress;
    @NotBlank
    @Size(max = 20)
    private String password;
    private String jobRole;
    private String interest;
    private String reCaptchaToken;
    private String localeName;
    @Size(max = 50)
    private String company;
    @Size(max = 50)
    private String department;
    @Size(max = 30)
    private String city;
    private String country;
    @Size(max = 13)
    private String phoneNumber;
    private String member;
    private String registrationType;
    private String timezone;
    private String hiraganaName;
    private Boolean isReCaptchaV2;

    // loyalty
    private Boolean acceptsAspireEnrollmentConsent;
    private Boolean isHealthcareProfessional;
    private Boolean isGovernmentEmployee;
    private Boolean isProhibitedFromAcceptingGifts;
    private Boolean acceptsAspireTermsAndConditions;

    // encryption
    private String ciphertext;

    // korea
    private Boolean websiteTermsOfUse;
    private Boolean eCommerceTermsOfUse;
    private Boolean thirdPartyTransferPersonalInfoMandatory;
    private Boolean thirdPartyTransferPersonalInfoOptional;
    private Boolean collectionAndUsePersonalInfoMandatory;
    private Boolean collectionAndUsePersonalInfoOptional;
    private Boolean collectionAndUsePersonalInfoMarketing;
    private Boolean overseasTransferPersonalInfoMandatory;
    private Boolean overseasTransferPersonalInfoOptional;
}
