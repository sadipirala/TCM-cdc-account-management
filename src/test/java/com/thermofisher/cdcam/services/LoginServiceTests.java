package com.thermofisher.cdcam.services;

import static org.junit.Assert.assertEquals;

import com.thermofisher.CdcamApplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
//@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest//(classes = CdcamApplication.class)
public class LoginServiceTests {

    @InjectMocks
    LoginService loginService;
    @Before
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
