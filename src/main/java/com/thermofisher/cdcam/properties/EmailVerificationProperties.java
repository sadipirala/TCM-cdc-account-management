package com.thermofisher.cdcam.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailVerificationProperties {

    /**
    * A boolean that indicates whether the Email Verification feature should be disabled globally.
     */
    @Getter
    @Value("${email-verification.enabled}")
    private static boolean isEnabled;

    /**
     * A boolean that indicates whether the Email Verification feature should be enabled globally.
     */
    @Getter
    @Value("${email-verification.global}")
    private static boolean isGlobal;

    /**
     * Property used in data.verifiedEmailDate for accounts that should not need to go through the
     * Email Verification flow.
     */
    @Getter
    @Value("${email-verification.defaultVerificationDate}")
    private static String defaultVerificationDate;

    /**
     * List of country codes that should prompt accounts to go through the Email Verification flow.
     */
    @Getter
    @Value("${email-verification.includedCountries}")
    private static List<String> includedCountries;

    /**
     * List of country codes that should be excluded from the Email Verification flow.
     */
    @Getter
    @Value("${email-verification.excludedCountries}")
    private static List<String> excludedCountries;
}
