package com.thermofisher.cdcam;

import com.thermofisher.cdcam.services.HashValidationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyString;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = HashValidationService.class)
public class HashValidationServiceTests {

    private Logger logger = LogManager.getLogger(HashValidationService.class);

    @Mock
    private HashValidationService hashValidationService = new HashValidationService();

    @Before
    public void setup() {
        ReflectionTestUtils.setField(hashValidationService, "logger", logger);
    }

    @Test
    public void isValidHash_ifGivenMatchingHashedStrings_returnTrue() {
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenCallRealMethod();
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenCallRealMethod();
        Assert.assertTrue(hashValidationService.isValidHash("sameText", "sameText"));
    }

    @Test
    public void isValidHash_ifGivenDifferentHashedStrings_returnFalse() {
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenCallRealMethod();
        Assert.assertFalse(hashValidationService.isValidHash("sameText", "otherText"));
    }

    @Test
    public void getHashedString_ifTheCorrectSecretKeyIsPassed_returnTheCorrectHash() {
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenCallRealMethod();
        ReflectionTestUtils.setField(hashValidationService, "algorithm", "HmacSHA1");
        String hashedText = hashValidationService.getHashedString("VGVzdA==", "Test");
        String expectedHash = "weah2UX1eNkjfaBUOmjBBmUp1yE=";
        Assert.assertEquals(hashedText, expectedHash);
    }

    @Test
    public void getHashedString_ifGivenAnInValidAlgorithm_returnNull() {
        Mockito.when(hashValidationService.getHashedString(anyString(), anyString())).thenCallRealMethod();
        ReflectionTestUtils.setField(hashValidationService, "algorithm", "incorrect");
        String hashedText = hashValidationService.getHashedString("Test", "Rand");
        Assert.assertNull(hashedText);
    }
}
