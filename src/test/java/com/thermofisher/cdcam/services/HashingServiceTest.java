package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.services.hashing.HashingService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HashingServiceTest {
    String HASHED_VALUE = "9D300D6CECD375DF73AFB16008977EAE";
    String PLAIN_TEXT_VALUE = "hashTest";

    @AfterAll
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

    @Test
    public void toMD5_GivenANullValue_returnException() throws NoSuchAlgorithmException {
        // when
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            HashingService.toMD5(null);
        });
    }

    @Test
    public void toMD5_GivenAnInvalidHashAlgorithm() throws NoSuchAlgorithmException {
        // given
        String PASSWORD_ALGORITHM = "X";
        ReflectionTestUtils.setField(HashingService.class, "PASSWORD_ALGORITHM", PASSWORD_ALGORITHM);

        Assertions.assertThrows(NoSuchAlgorithmException.class, () -> {
            HashingService.toMD5(PLAIN_TEXT_VALUE);
        });
    }
}
