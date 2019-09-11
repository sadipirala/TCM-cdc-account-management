package com.thermofisher.cdcam;

import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.services.HashValidationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyString;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = HashValidationService.class)
public class HashValidationServiceTests {

    @Mock
    private HashValidationService hashValidationService = new HashValidationService();
    @Mock
    SecretsManager secretsManager = new SecretsManager();

    @Test
    public void isValidHash_ifGivenMatchingHashedStrings_returnTrue() {
        Mockito.when(hashValidationService.getHashedString(anyString())).thenCallRealMethod();
        Mockito.when(hashValidationService.isValidHash(anyString(), anyString())).thenCallRealMethod();
        Assert.assertTrue(hashValidationService.isValidHash("sameText", "sameText"));
    }

    @Test
    public void isValidHash_ifGivenDifferentHashedStrings_returnFalse() {
        Mockito.when(hashValidationService.getHashedString(anyString())).thenCallRealMethod();
        Assert.assertFalse(hashValidationService.isValidHash("sameText", "otherText"));
    }

    @Test
    public void getHashedString_ifTheCorrectSecretKeyIsPassed_returnTheCorrectHash() {

        Mockito.when(hashValidationService.getSecretKeyFromSecretManager()).thenReturn("Test");
        Mockito.when(hashValidationService.getHashedString(anyString())).thenCallRealMethod();
        ReflectionTestUtils.setField(hashValidationService, "algorithm", "HmacSHA1");
        String hashedText = hashValidationService.getHashedString("Test");
        String expectedHash = "3/yMd4YBJt4TgB5d8Telp9N9sFA=";
        Assert.assertEquals(hashedText, expectedHash);
    }

    @Test
    public void getHashedString_ifGivenAnInValidAlgorithm_returnNull() {
        Mockito.when(hashValidationService.getSecretKeyFromSecretManager()).thenReturn("Test");
        Mockito.when(hashValidationService.getHashedString(anyString())).thenCallRealMethod();
        ReflectionTestUtils.setField(hashValidationService, "algorithm", "incorrect");
        String hashedText = hashValidationService.getHashedString("Test");
        Assert.assertNull(hashedText);
    }

    @Test
    public void getSecretKeyFromSecretManager_IfGivenAValidSecretIsFound_returnValue() {
        String secretProperties = "{\"cdc-secret-key\": \"Test\"}";
        ReflectionTestUtils.setField(hashValidationService, "algorithm", "HmacSHA1");
        ReflectionTestUtils.setField(hashValidationService, "secretsManager", secretsManager);
        ReflectionTestUtils.setField(hashValidationService, "secretName", "secretName");
        ReflectionTestUtils.setField(hashValidationService, "region", "region");
        Mockito.when(hashValidationService.getSecretKeyFromSecretManager()).thenCallRealMethod();
        Mockito.when(secretsManager.getSecret(anyString(),anyString())).thenReturn(secretProperties);

        String secretCDCKey = hashValidationService.getSecretKeyFromSecretManager();

        Assert.assertEquals(secretCDCKey,"Test");

    }
    @Test
    public void getSecretKeyFromSecretManager_IfAnInValidSecretIsFound_returnValue() {

        ReflectionTestUtils.setField(hashValidationService, "algorithm", "HmacSHA1");
        ReflectionTestUtils.setField(hashValidationService, "secretsManager", secretsManager);
        Mockito.when(hashValidationService.getSecretKeyFromSecretManager()).thenCallRealMethod();
        Mockito.when(secretsManager.getSecret(null,null)).thenReturn(null);


        Assert.assertNull(hashValidationService.getSecretKeyFromSecretManager());

    }
}
