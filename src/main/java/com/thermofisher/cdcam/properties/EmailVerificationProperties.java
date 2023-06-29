package com.thermofisher.cdcam.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailVerificationProperties {
    public final static String VERIFICATION_PENDING_FIELD = "data.verifiedEmailDate";
    public final static String ENFORCE_EMAIL_VERIFICATION_DATE = null;
    public final static String DEFAULT_VERIFIED_DATE = "0001-01-01";

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
            @Value("${email-verification.includedCountries}") List<String> includedCountries,
            @Value("${email-verification.excludedCountries}") List<String> excludedCountries
    ) {
        System.out.println(includedCountries.toString());
        EmailVerificationProperties.enabled = enabled;
        EmailVerificationProperties.global = global;
        EmailVerificationProperties.includedCountries = includedCountries;
        EmailVerificationProperties.excludedCountries = excludedCountries;
    }
}
