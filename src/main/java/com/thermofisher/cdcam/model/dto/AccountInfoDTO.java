package com.thermofisher.cdcam.model.dto;

import com.thermofisher.cdcam.model.Ciphertext;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
    private boolean receiveMarketingInformation;
    private boolean thirdPartyTransferPersonalInfoMandatory;
    private boolean thirdPartyTransferPersonalInfoOptional;
    private boolean collectionAndUsePersonalInfoMandatory;
    private boolean collectionAndUsePersonalInfoOptional;
    private boolean collectionAndUsePersonalInfoMarketing;
    private boolean overseasTransferPersonalInfoMandatory;
    private boolean overseasTransferPersonalInfoOptional;

    // honeypot
    @Null
    private String hname;
    @Null
    private String hemailAddress;

    public String getJobRoles() {
        return Objects.isNull(this.jobRoles) ? "" : String.join(",", this.jobRoles);
    }

    public String getInterests() {
        return Objects.isNull(this.interests) ? "" : String.join(",", this.interests);
    }

    public void setCiphertextData(Ciphertext ciphertext) {
        if (this.firstName == null && ciphertext.getFirstName() != null) {
            this.setFirstName(ciphertext.getFirstName());
        }

        if (this.lastName == null && ciphertext.getLastName() != null) {
            this.setLastName(ciphertext.getLastName());
        }

        this.setEmailAddress(ciphertext.getEmail());
    }
}
