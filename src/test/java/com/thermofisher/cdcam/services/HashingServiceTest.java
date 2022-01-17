package com.thermofisher.cdcam.services;

import static org.junit.Assert.assertEquals;

import java.security.NoSuchAlgorithmException;

import com.thermofisher.cdcam.services.hashing.HashingService;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HashingService.class)
public class HashingServiceTest {
    String HASHED_VALUE = "9D300D6CECD375DF73AFB16008977EAE";
    String PLAIN_TEXT_VALUE = "hashTest";

    @AfterClass
    public static void after() {
        String ALGORITHM = "MD5";
        ReflectionTestUtils.setField(HashingService.class, "PASSWORD_ALGORITHM", ALGORITHM);
    }

    @Test
    public void toMD5() throws NoSuchAlgorithmException {
        // given
        String PASSWORD_ALGORITHM = "MD5";
        String expectedResult = String.join(":", PASSWORD_ALGORITHM, HASHED_VALUE);
        ReflectionTestUtils.setField(HashingService.class, "PASSWORD_ALGORITHM", PASSWORD_ALGORITHM);

        // when
        String result = HashingService.toMD5(PLAIN_TEXT_VALUE);

        // then
        assertEquals(expectedResult, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toMD5_GivenANullValue_returnException() throws NoSuchAlgorithmException {
        // when
        HashingService.toMD5(null);
    }

    @Test(expected = NoSuchAlgorithmException.class)
    public void toMD5_GivenAnInvalidHashAlgorithm() throws NoSuchAlgorithmException{
        // given
        String PASSWORD_ALGORITHM = "X";
        ReflectionTestUtils.setField(HashingService.class, "PASSWORD_ALGORITHM", PASSWORD_ALGORITHM);

        // when
        HashingService.toMD5(PLAIN_TEXT_VALUE);
    }
}
