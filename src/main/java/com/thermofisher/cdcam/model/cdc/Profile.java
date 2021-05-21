package com.thermofisher.cdcam.model.cdc;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.thermofisher.cdcam.model.dto.MarketingConsentDTO;
import com.thermofisher.cdcam.model.dto.ProfileInfoDTO;
import com.thermofisher.cdcam.utils.Utils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder(builderClassName = "ProfileBuilder", toBuilder = true)
@Getter
@Setter
@JsonDeserialize(builder = Profile.ProfileBuilder.class)
public class Profile {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String country;
    private String city;
    private String locale;
    private Work work;
    private String timezone;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class ProfileBuilder {

    }

    @JsonIgnore
    public static Profile build(ProfileInfoDTO profileInfoDTO) {
        String city = null;
        String company = null;
        Work work = null;
        MarketingConsentDTO marketingConsentDTO = profileInfoDTO.getMarketingConsentDTO();
        if (Objects.nonNull(marketingConsentDTO)) {
            city = Utils.isNullOrEmpty(marketingConsentDTO.getCity()) ? null : marketingConsentDTO.getCity();
            company = Utils.isNullOrEmpty(marketingConsentDTO.getCompany()) ? null : marketingConsentDTO.getCompany();
            work = Objects.isNull(company) ? null : Work.builder().company(company).build();
        }
        return Profile.builder()
            .firstName(profileInfoDTO.getFirstName())
            .lastName(profileInfoDTO.getLastName())
            .city(city)
            .work(work)
            .build();
    }
}
