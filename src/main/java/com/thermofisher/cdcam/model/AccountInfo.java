package com.thermofisher.cdcam.model;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AccountInfo {
    private String uid;
    @NotBlank
    private String username;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String emailAddress;
    @NotBlank
    private String password;
    private String jobRole;
    private String interest;
    private String localeName;
    private String company;
    private String city;
    private String country;
    private String phoneNumber;
    private boolean marketingConsent;
    private String loginProvider;
    private int regAttempts;
    private String registrationType;
    private String timezone;
    private String hiraganaName;
    private String socialProviders;

    // loyalty
    private Boolean acceptsAspireEnrollmentConsent;
    private Boolean isHealthcareProfessional;
    private Boolean isGovernmentEmployee;
    private Boolean isProhibitedFromAcceptingGifts;
    private Boolean acceptsAspireTermsAndConditions;

    // korea
    private Boolean receiveMarketingInformation;
    private Boolean thirdPartyTransferPersonalInfoMandatory;
    private Boolean thirdPartyTransferPersonalInfoOptional;
    private Boolean collectionAndUsePersonalInfoMandatory;
    private Boolean collectionAndUsePersonalInfoOptional;
    private Boolean collectionAndUsePersonalInfoMarketing;
    private Boolean overseasTransferPersonalInfoMandatory;
    private Boolean overseasTransferPersonalInfoOptional;

    // openId provider
    private String openIdProviderId;

    //added for 580
    private String legacyUserName;
    private String previousEmail;

    @JsonIgnore
    public boolean isFederatedAccount() {
        return this.socialProviders.toLowerCase().contains("saml-");
    }
}
