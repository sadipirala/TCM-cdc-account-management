package com.thermofisher.cdcam.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EmailLocaleUtils.class)
public class EmailLocaleUtilsTests {
    
    @Test
    public void processLocaleForNotification_GivenALocaleIsSupported_ItShouldReturnTheLocaleInLowercaseLang_UppercaseCountry() {
        // given
        String supportedLocale = "es_MX";
        String supportedCountry = null;

        // when
        String result = EmailLocaleUtils.processLocaleForNotification(supportedLocale, supportedCountry);

        // then
        assertEquals(supportedLocale, result);
    }

    @Test
    public void processLocaleForNotification_GivenALocaleIsUnsupported_ItShouldJoinTheLowercasePassedLangInTheLocale_WithTheUppercasePassedCountry() {
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
