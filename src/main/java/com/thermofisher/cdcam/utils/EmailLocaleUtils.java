package com.thermofisher.cdcam.utils;

import java.util.Arrays;
import java.util.List;

public class EmailLocaleUtils {
    private final static String CHINA_LOCALE = "zh-cn";
    private final static String CHINA_TEMPLATE_MAPPING = "zh_CN";
    private final static String TAIWAN_LOCALE = "zh-tw";
    private final static String TFCOM_LOCALE_FOR_TAIWAN = "zt_TW";
    private final static String TAIWAN_TEMPLATE_MAPPING = "zh_TW";
    private final static String SUPPORTED_LOCALES = "en_US,de_DE,es_MX,es_AR,es_ES,es_CL,fr_FR,ko_KR,en_KR,ja_JP,en_JP,zh_CN,en_CN,zh_TW";

    /**
     * Processes a locale value and returns the best locale match to be used by an email.
     * 
     * @param locale the locale to be processed.
     * @param country the country to be concatenated to the language if a locale match is not found.
     * 
     * @return the locale as needed by an email, a matching locale for certain
     *         countries or a locale built by joining the passed locale and country.
     * <p> If it doesn't fit into any of ther previous explanations it will return the same passed locale. </p>
     *      
     */
    public static String processLocaleForNotification(String locale, String country) {
        if (isLocaleSupported(locale)) {
            return getLocaleAsNeededByANotification(locale);
        }

        if (isSpecialCountryLocale(locale)) { 
            return getCountryMatchingLocale(locale);
        }

        if (locale != null && country != null) {
            return joinLocaleAndCountry(locale, country);
        }
        
        return locale;
    }

    /**
     * Checks whether a locale is supported for an email.
     * 
     * @param locale the locale.
     * 
     * @return whether the locale is supported for an email.
     */
    private static boolean isLocaleSupported(String locale) {
        List<String> supportedLocaleList = Arrays.asList(SUPPORTED_LOCALES.toUpperCase().split(","));
        return supportedLocaleList.contains(locale.toUpperCase());
    }

    /**
     * Process the locale as needed by an email.
     * <p>This is just a failsafe operation to send the locale exactly as needed by an email,
     * which is as (e.g.) es_MX (lowercase_uppercase).</p>
     * 
     * @param locale the locale. Format: es_MX.
     * @return the locale as needed by an email.
     */
    private static String getLocaleAsNeededByANotification(String locale) {
        final int LANG = 0;
        final int COUNTRY = 1;
        String[] localeSections = locale.split("_");
        return joinLocaleAndCountry(localeSections[LANG], localeSections[COUNTRY]);
    }

    /**
     * Checks whether a locale needs a special handling for a specific country.
     * 
     * <p>Countries: <b>China and Taiwan.</b></p>
     * 
     * @param locale the locale.
     * 
     * @return whether a locale needs a special handling for a specific country.
     */
    private static boolean isSpecialCountryLocale(String locale) {
        return locale.toLowerCase().equals(CHINA_LOCALE) || isTaiwanLocale(locale);
    }

    /**
     * Process a specific country locale to be returned as needed by an email.
     * 
     * <p>Countries: <b>China and Taiwan.</b></p>
     * 
     * @param locale the locale.
     * 
     * @return the locale as needed by an email.
     */
    private static String getCountryMatchingLocale(String locale) {
        if (locale.toLowerCase().equals(CHINA_LOCALE)) {
            return CHINA_TEMPLATE_MAPPING;
        }

        if (isTaiwanLocale(locale)) {
            return TAIWAN_TEMPLATE_MAPPING;
        }

        return locale;
    }

    /**
     * Joins the lowercased locale and uppercased country in the following format. </br>
     * (e.g.) es_MX.
     * 
     * @param locale the locale.
     * @param country the country.
     * 
     * @return a joined locale and country.
     */
    private static String joinLocaleAndCountry(String locale, String country) {
        return String.format("%s_%s", locale.toLowerCase(), country.toUpperCase());
    }

    /**
     * Checks whether a locale is from Taiwan.
     * <p>Locales (non-case-sensitive): zh-tw, zt_tw.</p>
     * 
     * Keep in mind that for CDC, zt_TW is not a valid locale.
     * 
     * @param locale the locale.
     * @return whether a locale is from Taiwan.
     */
    private static boolean isTaiwanLocale(String locale) {
        return locale.toLowerCase().equals(TAIWAN_LOCALE) || locale.toLowerCase().equals(TFCOM_LOCALE_FOR_TAIWAN.toLowerCase());
    }
}
