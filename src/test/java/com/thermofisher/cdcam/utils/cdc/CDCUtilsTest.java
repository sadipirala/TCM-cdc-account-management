package com.thermofisher.cdcam.utils.cdc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(value = SpringExtension.class)
public class CDCUtilsTest {

    @Test
    public void isSecondaryDCSupported_ShouldReturnTrueIfEnvContains_qa4() {
        // given
        String env = "QA4-cn";

        // when
        boolean response = CDCUtils.isSecondaryDCSupported(env);

        // then
        assertTrue(response);
    }

    @Test
    public void isSecondaryDCSupported_ShouldReturnTrueIfEnvContains_qa1() {
        // given
        String env = "qa1-cn";

        // when
        boolean response = CDCUtils.isSecondaryDCSupported(env);

        // then
        assertTrue(response);
    }

    @Test
    public void isSecondaryDCSupported_ShouldReturnFalseIfEnvDoesNotContains_qa1_Or_qa4() {
        // given
        String env = "qa-cn";

        // when
        boolean response = CDCUtils.isSecondaryDCSupported(env);

        // then
        assertFalse(response);
    }

    @Test
    public void isSecondaryDCSupported_ShouldReturnTrueIfEnvContains_prod() {
        // given
        String env = "prod";

        // when
        boolean response = CDCUtils.isSecondaryDCSupported(env);

        // then
        assertTrue(response);
    }

    @Test
    public void isSecondaryDCSupported_ShouldReturnTrueIfEnvContains_cnprod() {
        // given
        String env = "cn.prod";

        // when
        boolean response = CDCUtils.isSecondaryDCSupported(env);

        // then
        assertTrue(response);
    }
}
