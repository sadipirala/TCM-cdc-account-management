package com.thermofisher.cdcam.model.cdc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import com.thermofisher.cdcam.model.dto.RegistrationDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Builder
@JsonDeserialize(builder = Registration.RegistrationBuilder.class)
public class Registration {
    private Japan japan;
    private China china;
    private Korea korea;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class RegistrationBuilder {
    }

    @JsonIgnore
    public static Registration build(RegistrationDTO registration){
        China china = China.builder().build();
        if (Objects.nonNull(registration.getChina())){
            String jobRoles = Objects.isNull(registration.getChina().getJobRole()) ? null : String.join(",", registration.getChina().getJobRole());
            String interests = Objects.isNull(registration.getChina().getInterest()) ? null : String.join(",", registration.getChina().getInterest());
            china.setInterest(interests);
            china.setJobRole(jobRoles);
            china.setPhoneNumber(registration.getChina().getPhoneNumber());
        }
        return Registration.builder()
                .china(china)
                .japan(registration.getJapan())
                .korea(registration.getKorea())
                .build();
    }
}
