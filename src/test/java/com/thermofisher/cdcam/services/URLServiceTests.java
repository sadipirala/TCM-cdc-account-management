package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.model.dto.CIPAuthDataDTO;
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
public class URLServiceTests {

    @InjectMocks
    URLService urlService;
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
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
        assertEquals("https://www.thermofisher.com?redirect_uri=redirect", paramString);
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
