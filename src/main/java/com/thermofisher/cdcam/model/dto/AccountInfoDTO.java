package com.thermofisher.cdcam.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Objects;

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
    @Size(min = 8, max = 20)
    private String password;
    private String[] jobRoles;
    private String[] interests;
    private String reCaptchaToken;
    private String localeName;
    @Size(max = 50)
    private String company;
    @Size(max = 30)
    private String city;
    private String country;
    @Size(max = 13)
    private String phoneNumber;
    private boolean marketingConsent;
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
    private Boolean receiveMarketingInformation;
    private Boolean thirdPartyTransferPersonalInfoMandatory;
    private Boolean thirdPartyTransferPersonalInfoOptional;
    private Boolean collectionAndUsePersonalInfoMandatory;
    private Boolean collectionAndUsePersonalInfoOptional;
    private Boolean collectionAndUsePersonalInfoMarketing;
    private Boolean overseasTransferPersonalInfoMandatory;
    private Boolean overseasTransferPersonalInfoOptional;

    public String getJobRoles() {
        return Objects.isNull(this.jobRoles) ? "" : String.join(",", this.jobRoles);
    }

    public String getInterests() {
        return Objects.isNull(this.interests) ? "" : String.join(",", this.interests);
    }
}
