package com.thermofisher.cdcam.utils.cdc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
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
