package com.thermofisher.cdcam.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class EmailVerificationProperties {
    private static boolean IS_ENABLED;
    private static boolean IS_GLOBAL;
    private static String DEFAULT_VERIFICATION_DATE;
    private static List<String> INCLUDED_COUNTRIES;
    private static List<String> EXCLUDED_COUNTRIES;

    @Autowired
    public EmailVerificationProperties(
            @Value("${email-verification.enabled}") boolean isEnabled,
            @Value("${email-verification.global}") boolean isGlobal,
            @Value("${email-verification.defaultVerificationDate}") String defaultVerificationDate,
            @Value("${email-verification.includedCountries}") String[] includedCountries,
            @Value("${email-verification.excludedCountries}") String[] excludedCountries
    ) {
        IS_ENABLED = isEnabled;
        IS_GLOBAL = isGlobal;
        DEFAULT_VERIFICATION_DATE = defaultVerificationDate;
        INCLUDED_COUNTRIES = Arrays.asList(includedCountries);
        EXCLUDED_COUNTRIES = Arrays.asList(excludedCountries);
    }

    public static boolean isEnabled() {
        return IS_ENABLED;
    }

    public static boolean isGlobal() {
        return IS_GLOBAL;
    }

    public static String getDefaultVerificationDate() {
        return DEFAULT_VERIFICATION_DATE;
    }

    public static List<String> getIncludedCountries() {
        return INCLUDED_COUNTRIES;
    }

    public static List<String> getExcludedCountries() {
        return EXCLUDED_COUNTRIES;
    }
}
