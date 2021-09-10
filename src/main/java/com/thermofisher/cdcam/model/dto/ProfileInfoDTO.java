package com.thermofisher.cdcam.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thermofisher.cdcam.utils.Utils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thermofisher.cdcam.model.AccountInfo;

@Builder
@Getter
@Setter
public class ProfileInfoDTO {
    @NotBlank
    private String uid;
    @NotBlank
    @Size(max = 30)
    private String firstName;
    @NotBlank
    @Size(max = 30)
    private String lastName;
    @Size(max = 50)
    private String username;
    @Size(max = 50)
    private String email;
    @JsonIgnore
    private String actualUsername;
    @JsonIgnore
    private String actualEmail;
    @JsonProperty("marketingConsent")
    private MarketingConsentDTO marketingConsentDTO;

    public static ProfileInfoDTO build(AccountInfo accountInfo) {
        MarketingConsentDTO marketingConsentDTO = MarketingConsentDTO.builder()
                .city(accountInfo.getCity())
                .company(accountInfo.getCompany())
                .country(accountInfo.getCountry())
                .consent(Boolean.parseBoolean(accountInfo.getMember()))
                .build();
        ProfileInfoDTO profileInfoDTO = ProfileInfoDTO.builder()
                .uid(accountInfo.getUid())
                .email(accountInfo.getEmailAddress())
                .firstName(accountInfo.getFirstName())
                .lastName(accountInfo.getLastName())
                .username(accountInfo.getUsername())
                .marketingConsentDTO(marketingConsentDTO)
                .build();

        return profileInfoDTO;
    }

    @JsonIgnore
    public boolean isALegacyProfile() {
        return !Utils.isAValidEmail(this.actualUsername);
    }
}
