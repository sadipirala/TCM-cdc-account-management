package com.thermofisher.cdcam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.enums.CookieType;
import com.thermofisher.cdcam.model.dto.CIPAuthDataDTO;
import com.thermofisher.cdcam.services.CookieService;
import com.thermofisher.cdcam.services.EncodeService;

import com.thermofisher.cdcam.utils.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class CookieServiceTests {
    Logger logger = LogManager.getLogger(this.getClass());
    private String COOKIE_CIP_AUTHDATA_VALID = "eyJjbGllbnRJZCI6ImNsaWVudElkIiwicmVkaXJlY3RVcmkiOiJyZWRpcmVjdFVyaSIsInN0YXRlIjoic3RhdGUiLCJzY29wZSI6InNjb3BlIiwicmVzcG9uc2VUeXBlIjoicmVzcG9uc2VUeXBlIn0=";
   
    @InjectMocks
    CookieService cookieService;

    @Mock
    EncodeService encodeService;

    @Value("${identity.cookie.cip-authdata.path}")
    String cipAuthdataPath;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(cookieService, "identityResetPasswordClientId", "id");
        ReflectionTestUtils.setField(cookieService, "identityResetPasswordRedirectUri", "redirectUri");
        ReflectionTestUtils.setField(cookieService, "identityResetPasswordResponseType", "responseType");
        ReflectionTestUtils.setField(cookieService, "identityResetPasswordScope", "scope");
        ReflectionTestUtils.setField(cookieService, "u", "https://www.dev3.thermofisher.com/order/catalog/en/US/adirect/lt?cmd=partnerMktLogin&newAccount=true&LoginData-referer=true&LoginData-ReturnURL=");
        ReflectionTestUtils.setField(cookieService, "tfHome", "https://www.thermofisher.com/");
    }

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
        String returnedDecodedString = "{\"client_id\":\"clientId\",\"redirect_uri\":\"redirectUri\",\"state\":\"state\",\"scope\":\"scope\",\"response_type\":\"responseType\"}";
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

    @Test
    public void buildDefaultCipAuthDataCookie_whenMethodIsCalled_thenShouldReturnCookieInBase64AsString() {
        // given
        CookieType cookieType = CookieType.RESET_PASSWORD;
        when(encodeService.encodeBase64(anyString())).thenReturn(COOKIE_CIP_AUTHDATA_VALID.getBytes());

        // when
        String result = cookieService.buildDefaultCipAuthDataCookie(cookieType);
        logger.info(result);

        // then
        assertTrue(!Utils.isNullOrEmpty(result));
    }
}
