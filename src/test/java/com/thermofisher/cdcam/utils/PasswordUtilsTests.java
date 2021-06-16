package com.thermofisher.cdcam.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PasswordUtils.class)
public class PasswordUtilsTests {

    @Test
    public void isPasswordValid_givenPasswordHasNoUppercase_ShouldReturnFalse() {
        // given
        String password = "p@ssw0rd";

        // when
        boolean result = PasswordUtils.isPasswordValid(password);

        // then
        assertFalse(result);
    }

    @Test
    public void isPasswordValid_givenPasswordHasNoLowercase_ShouldReturnFalse() {
        // given
        String password = "P@SSW0RD";

        // when
        boolean result = PasswordUtils.isPasswordValid(password);

        // then
        assertFalse(result);
    }

    @Test
    public void isPasswordValid_givenPasswordHasNoNumber_ShouldReturnFalse() {
        // given
        String password = "P@ssword";

        // when
        boolean result = PasswordUtils.isPasswordValid(password);

        // then
        assertFalse(result);
    }

    @Test
    public void isPasswordValid_givenPasswordHasNoSpecialCharacter_ShouldReturnFalse() {
        // given
        String password = "Passw0rd";

        // when
        boolean result = PasswordUtils.isPasswordValid(password);

        // then
        assertFalse(result);
    }
    
    @Test
    public void isPasswordValid_givenPasswordHasLengthLessThan8CharactersAndIsValid_ShouldReturnFalse() {
        // given
        String password = "P@ssw0r";

        // when
        boolean result = PasswordUtils.isPasswordValid(password);

        // then
        assertFalse(result);
    }

    @Test
    public void isPasswordValid_givenPasswordHasLengthMoreThan20CharactersAndIsValid_ShouldReturnFalse() {
        // given
        String password = "P@ssw0rdP@ssw0rdPass1";

        // when
        boolean result = PasswordUtils.isPasswordValid(password);

        // then
        assertFalse(result);
    }

    @Test
    public void isPasswordValid_givenPasswordLengthIs8CharactersAndValid_ShouldReturnTrue() {
        // given
        String password = "P@ssw0rd";

        // when
        boolean result = PasswordUtils.isPasswordValid(password);

        // then
        assertTrue(result);
    }

    @Test
    public void isPasswordValid_givenPasswordLengthIs20CharactersAndValid_ShouldReturnTrue() {
        // given
        String password = "P@ssw0rdP@ssw0rdPass";

        // when
        boolean result = PasswordUtils.isPasswordValid(password);

        // then
        assertTrue(result);
    }

    @Test
    public void isPasswordValid_givenPasswordLengthIsBetween8and20CharactersAndValid_ShouldReturnTrue() {
        // given
        String password = "P@ssw0rdPass";

        // when
        boolean result = PasswordUtils.isPasswordValid(password);

        // then
        assertTrue(result);
    }
}
