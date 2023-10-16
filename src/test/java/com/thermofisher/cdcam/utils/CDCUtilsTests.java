package com.thermofisher.cdcam.utils;

import com.thermofisher.cdcam.utils.cdc.CDCUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
