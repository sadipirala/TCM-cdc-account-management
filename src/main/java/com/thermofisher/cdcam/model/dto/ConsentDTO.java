package com.thermofisher.cdcam.model.dto;

import com.thermofisher.cdcam.model.OptionalRequiredConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Builder
@Getter
@Setter
@OptionalRequiredConstraint.List({
        @OptionalRequiredConstraint(
                optionalField = "city",
                requiredField = "marketingConsent",
                requiredBooleanValue = true,
                message = "Parameter city is required when marketingConsent is true."
        ),
        @OptionalRequiredConstraint(
                optionalField = "company",
                requiredField = "marketingConsent",
                requiredBooleanValue = true,
                message = "Parameter company is required when marketingConsent is true."
        )
})
public class ConsentDTO {

    @NotNull(message = "Parameter marketingConsent is required.")
    private Boolean marketingConsent;

    @NotBlank(message = "Parameter uid is required.")
    private String uid;

    private String city;

    private String company;
}
