package com.thermofisher.cdcam.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Component
public class EmailVerificationProperties {

    /**
    * A boolean that indicates whether the Email Verification feature should be disabled globally.
     */
    @Getter
    private static boolean enabled;

    /**
     * A boolean that indicates whether the Email Verification feature should be enabled globally.
     */
    @Getter
    private static boolean global;

    /**
     * Property used in data.verifiedEmailDate for accounts that should not need to go through the
     * Email Verification flow.
     */
    @Getter
    private static LocalDate defaultVerificationDate;

    /**
     * List of country codes that should prompt accounts to go through the Email Verification flow.
     */
    @Getter
    private static List<String> includedCountries;

    /**
     * List of country codes that should be excluded from the Email Verification flow.
     */
    @Getter
    private static List<String> excludedCountries;

    @Autowired
    public EmailVerificationProperties(
            @Value("${email-verification.enabled}") boolean enabled,
            @Value("${email-verification.global}") boolean global,
            @DateTimeFormat(pattern = "yyyy-MM-dd") @Value("${email-verification.defaultVerificationDate}") LocalDate defaultVerificationDate,
            @Value("${email-verification.includedCountries}") List<String> includedCountries,
            @Value("${email-verification.excludedCountries}") List<String> excludedCountries
    ) {
        EmailVerificationProperties.enabled = enabled;
        EmailVerificationProperties.global = global;
        EmailVerificationProperties.defaultVerificationDate = defaultVerificationDate;
        EmailVerificationProperties.includedCountries = includedCountries;
        EmailVerificationProperties.excludedCountries = excludedCountries;
    }
}
