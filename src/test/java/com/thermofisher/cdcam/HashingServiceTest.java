package com.thermofisher.cdcam;

import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.services.hashing.HashingService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.NoSuchAlgorithmException;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AccountBuilder.class)
public class HashingServiceTest {

    String HASHED_VALUE = "9D300D6CECD375DF73AFB16008977EAE";
    String PLAIN_TEXT_VALUE = "hashTest";
    String ALGORITHM = "MD5:";

    @InjectMocks
    HashingService hashingService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void hash_GivenAValueNotNUll_returnHashedString() throws NoSuchAlgorithmException{
        String PASSWORD_ALGORITHM = "MD5";
        ReflectionTestUtils.setField(hashingService,"PASSWORD_ALGORITHM",PASSWORD_ALGORITHM);
        String result = hashingService.hash(PLAIN_TEXT_VALUE).toUpperCase();
        Assert.assertEquals(result, HASHED_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void hash_GivenANullValue_returnException() throws NoSuchAlgorithmException {
        hashingService.hash(null);
    }

    @Test(expected = NoSuchAlgorithmException.class)
    public void hash_GivenAnInvalidHashAlgorithm() throws NoSuchAlgorithmException{
        String PASSWORD_ALGORITHM = "X";
        ReflectionTestUtils.setField(hashingService,"PASSWORD_ALGORITHM",PASSWORD_ALGORITHM);
        hashingService.hash(PLAIN_TEXT_VALUE);
    }

    @Test
    public void concat_GivenValue_returnConcatenatedString() {
        String result = hashingService.concat(HASHED_VALUE);
        Assert.assertEquals(result, ALGORITHM.concat(HASHED_VALUE));
    }
}
