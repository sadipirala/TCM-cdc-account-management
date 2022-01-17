package com.thermofisher.cdcam.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EmailLocaleUtils.class)
public class EmailLocaleUtilsTests {

    @Test
    public void processLocaleForNotification_GivenLocaleIsNull_ShouldReturnUSEnlgishLocale() {
        // given
        String locale = null;
        String country = "mx";

        // when
        String result = EmailLocaleUtils.processLocaleForNotification(locale, country);

        // then
        assertEquals(EmailLocaleUtils.US_ENGLISH_LOCALE, result);
    }

    @Test
    public void processLocaleForNotification_GivenLocaleIsEmpty_ShouldReturnUSEnlgishLocale() {
        // given
        String locale = "";
        String country = "MX";

        // when
        String result = EmailLocaleUtils.processLocaleForNotification(locale, country);

        // then
        assertEquals(EmailLocaleUtils.US_ENGLISH_LOCALE, result);
    }

    @Test
    public void processLocaleForNotification_GivenCountryIsNull_ShouldReturnUSEnlgishLocale() {
        // given
        String locale = "en";
        String country = null;

        // when
        String result = EmailLocaleUtils.processLocaleForNotification(locale, country);

        // then
        assertEquals(EmailLocaleUtils.US_ENGLISH_LOCALE, result);
    }

    @Test
    public void processLocaleForNotification_GivenCountryIsEmpty_ShouldReturnUSEnlgishLocale() {
        // given
        String locale = "en";
        String country = "";

        // when
        String result = EmailLocaleUtils.processLocaleForNotification(locale, country);

        // then
        assertEquals(EmailLocaleUtils.US_ENGLISH_LOCALE, result);
    }
    
    @Test
    public void processLocaleForNotification_GivenALocaleHasValidFormat_ShouldReturnSameLocale() {
        // given
        String locale = "es_MX";
        String country = "MX";

        // when
        String result = EmailLocaleUtils.processLocaleForNotification(locale, country);

        // then
        assertEquals(locale, result);
    }

    @Test
    public void processLocaleForNotification_GivenLocaleAndCountryHaveLengthOf2_ItShouldJoinThemAsLowercaseLocale_UppercaseCountry() {
        // given
        String supportedLocale = "en";
        String supportedCountry = "US";
        String expectedResult = "en_US";

        // when
        String result = EmailLocaleUtils.processLocaleForNotification(supportedLocale, supportedCountry);

        // then
        assertEquals(expectedResult, result);
    }

    @Test
    public void processLocaleForNotification_GivenTheLocaleIsFromChinaAsNeededByCDC_ItShouldReturnTheLocaleSupportedForChinaEmails() {
        // given
        String chinaLocale = "zh-cn";
        String country = "CN";
        String expectedResult = "zh_CN";

        // when
        String result = EmailLocaleUtils.processLocaleForNotification(chinaLocale, country);

        // then
        assertEquals(expectedResult, result);
    }

    @Test
    public void processLocaleForNotification_GivenTheLocaleIsFromTaiwanAsSentByTFCOM_ItShouldReturnTheLocaleSupportedForTaiwanEmails() {
        // given
        String TFCOM_taiwanLocale = "zt_TW";
        String country = "TW";
        String expectedResult = "zh_TW";

        // when
        String result = EmailLocaleUtils.processLocaleForNotification(TFCOM_taiwanLocale, country);

        // then
        assertEquals(expectedResult, result);
    }

    @Test
    public void processLocaleForNotification_GivenTheLocaleIsFromTaiwanAsNeededByCDC_ItShouldReturnTheLocaleSupportedForTaiwanEmails() {
        // given
        String taiwanLocale = "zh-tw";
        String country = "TW";
        String expectedResult = "zh_TW";

        // when
        String result = EmailLocaleUtils.processLocaleForNotification(taiwanLocale, country);

        // then
        assertEquals(expectedResult, result);
    }
}
