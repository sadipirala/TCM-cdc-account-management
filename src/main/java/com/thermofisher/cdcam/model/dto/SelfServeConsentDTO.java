package com.thermofisher.cdcam.model.dto;

import com.thermofisher.cdcam.model.OptionalRequiredWhenTrueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
@Getter
@Setter
@OptionalRequiredWhenTrueConstraint.List({
        @OptionalRequiredWhenTrueConstraint(
                optionalField = "city",
                requiredBooleanField = "marketingConsent",
                message = "Parameter city is required when marketingConsent is true."
        ),
        @OptionalRequiredWhenTrueConstraint(
                optionalField = "company",
                requiredBooleanField = "marketingConsent",
                message = "Parameter company is required when marketingConsent is true."
        )
})
public class SelfServeConsentDTO {

    @NotNull(message = "Parameter marketingConsent is required.")
    private Boolean marketingConsent;

    @NotBlank(message = "Parameter uid is required.")
    private String uid;

    private String city;

    private String company;
}
