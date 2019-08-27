package com.thermofisher.cdcam;

import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.services.HashValidationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = HashValidationService.class)
public class HashValidationServiceTests {


    private HashValidationService hashValidationService = new HashValidationService();
    @Test
    public void isValidHash_ifGivenMatchingHashedString_returnTrue(){
        Assert.assertTrue(hashValidationService.isValidHash("sameText","sameText"));
    }
    @Test
    public void isValidHash_ifGivenDifferentHashedString_returnFalse(){
        Assert.assertFalse(hashValidationService.isValidHash("sameText","otherText"));
    }
    @Test
    public void getHashedString_ifTheCorrectSecretKeyIsPassed_returnTheCorrectHash(){
        String hashedText = hashValidationService.getHashedString("Test","CVfTtNBGQtO1lqWsjxdwfIf6Egh5owIwFfQ8tVu+iKw=","HmacSHA1");
        String expectedHash = "y0JdZwjTBmXWlJOTGma5bw8QSW4=";
        Assert.assertEquals(hashedText, expectedHash);
    }
    @Test
    public void getHashedString_ifAnInValidAlgorithm_returnNull(){
        String hashedText = hashValidationService.getHashedString("Test","CVfTtNBGQtO1lqWsjxdwfIf6Egh5owIwFfQ8tVu+iKw=","invalidAlgorithm");
        Assert.assertNull(hashedText);
    }
}
