package com.thermofisher.cdcam.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailLocaleUtils {
    private final static String VALID_LOCALE_REGEX = "^[a-zA-Z]{2}_[a-zA-Z]{2}$";
    private final static String CDC_CHINA_LOCALE = "zh-cn";
    private final static String CHINA_LOCALE_FOR_EMAIL = "zh_CN";
    private final static String CDC_TAIWAN_LOCALE = "zh-tw";
    private final static String TFCOM_TAIWAN_LOCALE = "zt_tw";
    private final static String TAIWAN_LOCALE_FOR_EMAIL = "zh_TW";
    public final static String US_ENGLISH_LOCALE = "en_US";

    /**
     * Processes a locale value and returns the best locale match to be used by an email.
     *
     * @param locale  the locale to be processed.
     * @param country the country to be concatenated to the language if a locale match is not found.
     * @return the locale as needed by an email, a matching locale for certain
     * countries or a locale built by joining the passed locale and country.
     * <p> If it doesn't fit into any of ther previous explanations it will return the same passed locale. </p>
     */
    public static String processLocaleForNotification(String locale, String country) {
        if (StringUtils.isBlank(locale) || StringUtils.isBlank(country)) {
            return US_ENGLISH_LOCALE;
        }

        if (isSpecialCountryLocale(locale)) {
            return getSpecialCountryLocaleForEmail(locale);
        }

        if (isLocaleFormatValid(locale)) {
            return parseLocaleForEmails(locale);
        }

        if (locale.length() == 2 && country.length() == 2) {
            return joinLocaleAndCountry(locale, country);
        }

        return locale;
    }

    private static boolean isLocaleFormatValid(String locale) {
        Pattern pattern = Pattern.compile(VALID_LOCALE_REGEX);
        Matcher matcher = pattern.matcher(locale);
        return matcher.find();
    }

    /**
     * Process the locale as needed by an email.
     * <p>(e.g.) es_MX (lowercase_uppercase).</p>
     *
     * @param locale the locale. Format: es_MX.
     * @return the locale as needed by an email.
     */
    private static String parseLocaleForEmails(String locale) {
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
     * @return whether a locale needs a special handling for a specific country.
     */
    private static boolean isSpecialCountryLocale(String locale) {
        return isCdcChinaLocale(locale) || isTaiwanLocale(locale);
    }

    /**
     * Process a specific country locale to be returned as needed by an email.
     *
     * <p>Countries: <b>China and Taiwan.</b></p>
     *
     * @param locale the locale.
     * @return the locale as needed by an email.
     */
    private static String getSpecialCountryLocaleForEmail(String locale) {
        if (isCdcChinaLocale(locale)) {
            return CHINA_LOCALE_FOR_EMAIL;
        }

        if (isTaiwanLocale(locale)) {
            return TAIWAN_LOCALE_FOR_EMAIL;
        }

        return locale;
    }

    /**
     * Joins the locale and country in the following format. </br>
     * (e.g.) es_MX.
     *
     * @param locale  the locale.
     * @param country the country.
     * @return a joined locale and country.
     */
    private static String joinLocaleAndCountry(String locale, String country) {
        return String.format("%s_%s", locale.toLowerCase(), country.toUpperCase());
    }

    /**
     * Checks whether a locale is from China as needed by CDC.
     * <p>Locale as needed by CDC (lowercase): zh-cn.</p>
     *
     * @param locale the locale.
     * @return whether a locale is from China.
     */
    private static boolean isCdcChinaLocale(String locale) {
        return locale.toLowerCase().equals(CDC_CHINA_LOCALE);
    }

    /**
     * Checks whether a locale is from Taiwan.
     * <p>Locales (lowercase): zh-tw, zt_tw.</p>
     * <p>
     * tf.com's zt_TW locale is not a valid for CDC.
     *
     * @param locale the locale.
     * @return whether a locale is from Taiwan.
     */
    private static boolean isTaiwanLocale(String locale) {
        return locale.toLowerCase().equals(CDC_TAIWAN_LOCALE) || locale.toLowerCase().equals(TFCOM_TAIWAN_LOCALE);
    }
}
