package com.thermofisher.cdcam.services;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.enums.CookieType;
import com.thermofisher.cdcam.model.dto.CIPAuthDataDTO;

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
    private String COOKIE_CIP_AUTHDATA_VALID = "eyJjbGllbnRJZCI6ImNsaWVudElkIiwicmVkaXJlY3RVcmkiOiJyZWRpcmVjdFVyaSIsInN0YXRlIjoic3RhdGUiLCJzY29wZSI6InNjb3BlIiwicmVzcG9uc2VUeXBlIjoicmVzcG9uc2VUeXBlIn0=";
    private String CUSTOM_RP_CIP_AUTHDATA_COOKIE = "eyJjbGllbnRfaWQiOiI3bnp2N0ptSlQtM1IxWjBGWkVxX1Y1RTgiLCJyZWdSZWRpcmVjdFVyaSI6Imh0dHBzOi8vd3d3LnFhNC50aGVybW9maXNoZXIuY29tL2F1dGgvbG9naW4vY3JlYXRlIiwic2lnbkluUmVkaXJlY3RVcmkiOiJodHRwczovL3d3dy5xYTQudGhlcm1vZmlzaGVyLmNvbS9hdXRoL2xvZ2luIiwicmV0dXJuVXJsIjoiaHR0cHM6Ly93d3cucWE0LnRoZXJtb2Zpc2hlci5jb20ifQ==";
   
    @InjectMocks
    CookieService cookieService;

    @Mock
    EncodeService encodeService;

    @Value("${identity.registration.get-login-endpoint.path}")
    String getOidcLoginEndpointPath;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(cookieService, "identityResetPasswordClientId", "id");
        ReflectionTestUtils.setField(cookieService, "identityResetPasswordRedirectUri", "redirectUri");
        ReflectionTestUtils.setField(cookieService, "identityResetPasswordResponseType", "responseType");
        ReflectionTestUtils.setField(cookieService, "identityResetPasswordScope", "scope");
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
        String [] txtCookieArray = cookieService.createCIPAuthDataCookie(cipAuthData, getOidcLoginEndpointPath).split(";");
        String txtCookie = txtCookieArray[0].substring(13);

        // then
        assertTrue(txtCookie.matches("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$"));
    }

    @Test
    public void decodeCIPAuthDataCookie_givenACodedStringInBase64_whenMethodIsCalled_thenReturnDecodedString() {
        // given
        String cookieValue = "eyJjbGllbnRJZCI6ImNsaWVudElkIiwicmVkaXJlY3RVcmkiOiJyZWRpcmVjdFVyaSIsInN0YXRlIjoic3RhdGUiLCJzY29wZSI6InNjb3BlIiwicmVzcG9uc2VUeXBlIjoicmVzcG9uc2VUeXBlIn0=";
        byte[] cookieStringBytes = cookieValue.getBytes();
        String returnedDecodedString = "{\"client_id\":\"clientId\",\"redirect_uri\":\"redirectUri\",\"state\":\"state\",\"scope\":\"scope\",\"response_type\":\"responseType\"}";
        when(encodeService.decodeBase64(cookieStringBytes)).thenReturn(returnedDecodedString);

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
        String cookieMock = "eyJjbGllbnRJZCI6ImNsaWVudElkIiwicmVkaXJlY3RVcmkiOiJyZWRpcmVjdFVyaSIsInN0YXRlIjoic3RhdGUiLCJzY29wZSI6InNjb3BlIiwicmVzcG9uc2VUeXBlIjoicmVzcG9uc2VUeXBlIn0=";
        when(encodeService.encodeBase64(anyString())).thenReturn(COOKIE_CIP_AUTHDATA_VALID.getBytes());

        // when
        String result = cookieService.buildDefaultCipAuthDataCookie(cookieType);
        
        // then
        assertEquals(cookieMock, result);
    }

    @Test
    // given custom rp params are present in the cip_authdata cookie, it should return the signInRedirectUri property
    public void returnCustomSignInRedirectUri() {
        // given
        byte[] cookieStringBytes = CUSTOM_RP_CIP_AUTHDATA_COOKIE.getBytes();
        String decodedCookieString = "{\"client_id\":\"client_id\",\"regRedirectUri\":\"regRedirectUri\",\"signInRedirectUri\":\"signInRedirectUri\",\"returnUrl\":\"returnUrl\"}";
        when(encodeService.decodeBase64(cookieStringBytes)).thenReturn(decodedCookieString);

        // when
        CIPAuthDataDTO cipAuthDataDTO = cookieService.decodeCIPAuthDataCookie(CUSTOM_RP_CIP_AUTHDATA_COOKIE);

        // then
        assertEquals("client_id", cipAuthDataDTO.getClientId());
        assertEquals("regRedirectUri", cipAuthDataDTO.getRegRedirectUri());
        assertEquals("signInRedirectUri", cipAuthDataDTO.getSignInRedirectUri());
        assertEquals("returnUrl", cipAuthDataDTO.getReturnUrl());
        assertNull(cipAuthDataDTO.getResponseType());
        assertNull(cipAuthDataDTO.getState());
        assertNull(cipAuthDataDTO.getScope());
        assertNull(cipAuthDataDTO.getRedirectUri());
    }
}
