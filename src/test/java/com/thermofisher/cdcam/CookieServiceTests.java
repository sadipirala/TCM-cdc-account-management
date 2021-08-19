package com.thermofisher.cdcam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.model.dto.CIPAuthDataDTO;
import com.thermofisher.cdcam.services.CookieService;
import com.thermofisher.cdcam.services.EncodeService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class CookieServiceTests {
   
    @InjectMocks
    CookieService cookieService;

    @Mock
    EncodeService encodeService;

    @Value("${identity.cookie.cip-authdata.path}")
    String cipAuthdataPath;

    @Test
    public void createCIPAuthDataCookie_givenACIPAuthDataDTO_whenMethodIsCalled_thenReturnCookieTxtWithBase64Value() {
        // given
        CIPAuthDataDTO cipAuthData = CIPAuthDataDTO.builder()
                .clientId("clientId")
                .redirectUri("redirectUri")
                .responseType("responseType")
                .scope("scope")
                .state("state")
                .build();
        when(encodeService.encodeBase64(anyString())).thenReturn(new byte[] {});
        
        // when
        String [] txtCookieArray = cookieService.createCIPAuthDataCookie(cipAuthData, cipAuthdataPath).split(";");
        String txtCookie = txtCookieArray[0].substring(13);

        // then
        assertTrue(txtCookie.matches("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$"));
    }

    @Test
    public void decodeCIPAuthDataCookie_givenACodedStringInBase64_whenMethodIsCalled_thenReturnDecodedString() {
        // given
        CIPAuthDataDTO cipAuthData = CIPAuthDataDTO.builder()
                .clientId("clientId")
                .redirectUri("redirectUri")
                .responseType("responseType")
                .scope("scope")
                .state("state")
                .build();
        String cookieValue = "eyJjbGllbnRJZCI6ImNsaWVudElkIiwicmVkaXJlY3RVcmkiOiJyZWRpcmVjdFVyaSIsInN0YXRlIjoic3RhdGUiLCJzY29wZSI6InNjb3BlIiwicmVzcG9uc2VUeXBlIjoicmVzcG9uc2VUeXBlIn0=";
        byte[] cookieStringBytes = cookieValue.getBytes();
        String returnedDecodedString = "{\"clientId\":\"clientId\",\"redirectUri\":\"redirectUri\",\"state\":\"state\",\"scope\":\"scope\",\"responseType\":\"responseType\"}";
        when(encodeService.decodeBase64(cookieStringBytes)).thenReturn(returnedDecodedString);
        //when(jsonParserService.parseStringToCipAuthDataDto("{\"clientId\":\"clientId\",\"redirectUri\":\"redirectUri\",\"state\":\"state\",\"scope\":\"scope\",\"responseType\":\"responseType\"}")).thenReturn(cipAuthData);

        // when
        CIPAuthDataDTO cipAuthDataDTO = cookieService.decodeCIPAuthDataCookie(cookieValue);

        // then
        assertEquals(cipAuthDataDTO.getClientId(), "clientId");
        assertEquals(cipAuthDataDTO.getRedirectUri(), "redirectUri");
        assertEquals(cipAuthDataDTO.getState(), "state");
        assertEquals(cipAuthDataDTO.getScope(), "scope");
        assertEquals(cipAuthDataDTO.getResponseType(), "responseType");
    }
}
