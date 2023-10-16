package com.thermofisher.cdcam.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LoginServiceTests {

    @InjectMocks
    LoginService loginService;
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void generateDefaultLoginUrl_whenMethodIsCalled_thenReturnTheDefaultLoginUrl() {
        // given
        String redirectUrl = "https://www.dev3.thermofisher.com/global-registration/confirmation";
        String expectedString = "https://www.dev3.thermofisher.com/auth/login?returnUrl=https://www.dev3.thermofisher.com/global-registration/confirmation";
        ReflectionTestUtils.setField(loginService,"loginEndpoint","https://www.dev3.thermofisher.com/auth/login");

        // when
        String expectedGeneratedString = loginService.generateDefaultLoginUrl(redirectUrl);

        // then
        assertEquals(expectedString, expectedGeneratedString);
    }
}
