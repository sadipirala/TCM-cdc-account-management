package com.thermofisher.cdcam.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.utils.Utils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
    private String country;
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
                .consent(accountInfo.isMarketingConsent())
                .build();

        return ProfileInfoDTO.builder()
                .uid(accountInfo.getUid())
                .email(accountInfo.getEmailAddress())
                .firstName(accountInfo.getFirstName())
                .lastName(accountInfo.getLastName())
                .username(accountInfo.getUsername())
                .marketingConsentDTO(marketingConsentDTO)
                .country(accountInfo.getCountry())
                .build();
    }

    @JsonIgnore
    public boolean isALegacyProfile() {
        return !Utils.isAValidEmail(this.actualUsername);
    }
}
