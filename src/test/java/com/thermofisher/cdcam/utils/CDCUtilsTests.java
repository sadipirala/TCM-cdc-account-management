package com.thermofisher.cdcam.utils;

import com.thermofisher.cdcam.utils.cdc.CDCUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
@SpringBootTest//(classes = CDCUtils.class)
public class CDCUtilsTests {

    @Test
    public void isSecondaryDCSupported_givenQA4Environment_whenMethodIsCalled_thenShouldReturnTrueIfEnvironmentIsSupported() {
        // given
        String env = "qa4";

        // when
        boolean result = CDCUtils.isSecondaryDCSupported(env);

        // then
        assertTrue(result);
    }

    @Test
    public void isSecondaryDCSupported_givenQA1Environment_whenMethodIsCalled_thenShouldReturnTrueIfEnvironmentIsSupported() {
        // given
        String env = "qa1";

        // when
        boolean result = CDCUtils.isSecondaryDCSupported(env);

        // then
        assertTrue(result);
    }

    @Test
    public void isSecondaryDCSupported_givenPRODEnvironment_whenMethodIsCalled_thenShouldReturnTrueIfEnvironmentIsSupported() {
        // given
        String env = "prod";

        // when
        boolean result = CDCUtils.isSecondaryDCSupported(env);

        // then
        assertTrue(result);
    }

    @Test
    public void isSecondaryDCSupported_givenAnEnvironment_whenMethodIsCalled_thenShouldReturnFalseIfEnvironmentIsUnsupported() {
        // given
        String env = "test";

        // when
        boolean result = CDCUtils.isSecondaryDCSupported(env);

        // then
        assertFalse(result);
    }
}
