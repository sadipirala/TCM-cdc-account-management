package com.thermofisher.cdcam;

import java.security.NoSuchAlgorithmException;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.services.hashing.HashingService;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class HashingServiceTest {
    String HASHED_VALUE = "9D300D6CECD375DF73AFB16008977EAE";
    String PLAIN_TEXT_VALUE = "hashTest";
    String ALGORITHM = "MD5:";

    @Test
    public void hash_GivenAValueNotNUll_returnHashedString() throws NoSuchAlgorithmException{
        // given
        String PASSWORD_ALGORITHM = "MD5";
        ReflectionTestUtils.setField(HashingService.class, "PASSWORD_ALGORITHM", PASSWORD_ALGORITHM);

        // when
        String result = HashingService.hash(PLAIN_TEXT_VALUE).toUpperCase();

        // then
        Assert.assertEquals(result, HASHED_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void hash_GivenANullValue_returnException() throws NoSuchAlgorithmException {
        // when
        HashingService.hash(null);
    }

    @Test(expected = NoSuchAlgorithmException.class)
    public void hash_GivenAnInvalidHashAlgorithm() throws NoSuchAlgorithmException{
        // given
        String PASSWORD_ALGORITHM = "X";
        ReflectionTestUtils.setField(HashingService.class,"PASSWORD_ALGORITHM",PASSWORD_ALGORITHM);

        // when
        HashingService.hash(PLAIN_TEXT_VALUE);
    }

    @Test
    public void concat_GivenValue_returnConcatenatedString() {
        // when
        String result = HashingService.concat(HASHED_VALUE);

        // then
        Assert.assertEquals(result, ALGORITHM.concat(HASHED_VALUE));
    }
}
