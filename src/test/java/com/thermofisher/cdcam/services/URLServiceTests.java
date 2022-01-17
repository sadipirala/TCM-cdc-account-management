package com.thermofisher.cdcam.services;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.model.dto.CIPAuthDataDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class URLServiceTests {

    @InjectMocks
    URLService urlService;

    @Test
    public void queryParamMapper_givenAJsonString_whenMethodIsCalled_thenReturnStringOfParams() {
        // given
        CIPAuthDataDTO cipAuthData = generateCipAuthDataDTO();
        ReflectionTestUtils.setField(urlService, "identityAuthorizeEndpoint", "https://www.thermofisher.com");

        // when
        String paramString = urlService.queryParamMapper(cipAuthData);

        // then
        String EXPECTED_STRING = "https://www.thermofisher.com?client_id=clientId&redirect_uri=redirectUri&state=state&scope=scope&response_type=responseType";
        assertEquals(EXPECTED_STRING, paramString);
    }

    @Test
    public void queryParamMapper_givenAnObjectWithEmptyValue_whenMethodIsCalled_thenShouldSkipTheObjectKey() {
        // given
        CIPAuthDataDTO cipAuthData = CIPAuthDataDTO.builder()
            .clientId("")
            .redirectUri("redirect")
            .build();
        ReflectionTestUtils.setField(urlService, "identityAuthorizeEndpoint", "https://www.thermofisher.com");

        // when
        String paramString = urlService.queryParamMapper(cipAuthData);

        // then
        String EXPECTED_STRING = "https://www.thermofisher.com?redirect_uri=redirect&state=null&scope=null&response_type=null";
        assertEquals(EXPECTED_STRING, paramString);
    }

    public CIPAuthDataDTO generateCipAuthDataDTO() {
        CIPAuthDataDTO cipAuthData = CIPAuthDataDTO.builder()
                .clientId("clientId")
                .redirectUri("redirectUri")
                .responseType("responseType")
                .scope("scope")
                .state("state")
                .build();
        return cipAuthData;
    }
}
