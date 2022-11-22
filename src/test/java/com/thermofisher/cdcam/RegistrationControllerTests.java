package com.thermofisher.cdcam;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gigya.socialize.GSKeyNotFoundException;
import com.google.gson.JsonParseException;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.controller.RegistrationController;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.OpenIdRelyingParty;
import com.thermofisher.cdcam.model.dto.CIPAuthDataDTO;
import com.thermofisher.cdcam.services.CookieService;
import com.thermofisher.cdcam.services.EncodeService;
import com.thermofisher.cdcam.services.GigyaService;
import com.thermofisher.cdcam.services.LoginService;
import com.thermofisher.cdcam.services.URLService;
import com.thermofisher.cdcam.utils.Utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
@ContextConfiguration(classes = RegistrationController.class)
@TestPropertySource(locations = "/application-test.properties", properties = {"tf.home=https://www.thermofisher.com/"})
public class RegistrationControllerTests {
    private String CLIENT_ID = "1000000";
    private String REDIRECT_URL = "http://example.com";
    private String STATE = "state";
    private String RESPONSE_TYPE = "responseType";
    private String SCOPE = "scope";
    private String COOKIE_CIP_AUTHDATA_VALID = "eyJjbGllbnRJZCI6ImNsaWVudElkIiwicmVkaXJlY3RVcmkiOiJyZWRpcmVjdFVyaSIsInN0YXRlIjoic3RhdGUiLCJzY29wZSI6InNjb3BlIiwicmVzcG9uc2VUeXBlIjoicmVzcG9uc2VUeXBlIn0=";
    private String COOKIE_CIP_AUTHDATA_INVALID = "eyJyZWRpcmVjdFVyaSI6InJlZGlyZWN0VXJpIiwic3RhdGUiOiJzdGF0ZSIsInNjb3BlIjoic2NvcGUiLCJyZXNwb25zZVR5cGUiOiJyZXNwb25zZVR5cGUifQ==";
    private String CUSTOM_RP_CIP_AUTHDATA_COOKIE = "eyJjbGllbnRfaWQiOiI3bnp2N0ptSlQtM1IxWjBGWkVxX1Y1RTgiLCJyZWdSZWRpcmVjdFVyaSI6Imh0dHBzOi8vd3d3LnFhNC50aGVybW9maXNoZXIuY29tL2F1dGgvbG9naW4vY3JlYXRlIiwic2lnbkluUmVkaXJlY3RVcmkiOiJodHRwczovL3d3dy5xYTQudGhlcm1vZmlzaGVyLmNvbS9hdXRoL2xvZ2luIiwicmV0dXJuVXJsIjoiaHR0cHM6Ly93d3cucWE0LnRoZXJtb2Zpc2hlci5jb20ifQ==";
    private String CREATE_ACCOUNT_ENDPOINT_PATH = "/api-gateway/accounts";
    private String GET_LOGIN_ENDPOINT_PATH = "/api-gateway/identity/registration/redirect/login";
    private boolean IS_SIGN_IN_URL = true;

    @InjectMocks
    RegistrationController registrationController;

    @Mock
    GigyaService gigyaService;

    @Mock
    CookieService cookieService;

    @Mock
    URLService urlService;

    @Mock
    CIPAuthDataDTO cipAuthData;

    @Mock
    EncodeService encodeService;

    @Mock
    LoginService loginService;

    @Test
    public void getRPRegistrationConfig_GivenMethodCalled_WhenParametersAreValid_ThenShouldReturnFoundStatusAndCIP_AUTDATAShouldBePresentInHeaders() throws Exception {
        // given
        int NO_OF_COOKIES = 2;
        ReflectionTestUtils.setField(registrationController, "createAccountEndpointPath", CREATE_ACCOUNT_ENDPOINT_PATH);
        ReflectionTestUtils.setField(registrationController, "getOidcLoginEndpointPath", GET_LOGIN_ENDPOINT_PATH);
        List<String> redirectUris = new ArrayList<>(Arrays.asList("http://example.com", "http://example2.com"));
        String description = "Description";
        OpenIdRelyingParty openIdRelyingParty = OpenIdRelyingParty.builder()
            .clientId(CLIENT_ID)
            .description(description)
            .redirectUris(redirectUris)
            .build();
        String params = "?state=state&redirect_uri=redirect";
        when(encodeService.encodeUTF8(anyString())).thenReturn(URLDecoder.decode(params, StandardCharsets.UTF_8.toString()));
        when(gigyaService.getRP(anyString())).thenReturn(openIdRelyingParty);
        when(cookieService.createCIPAuthDataCookie(any(CIPAuthDataDTO.class), anyString())).thenReturn(RandomStringUtils.randomAlphanumeric(10));

        // when
        ResponseEntity<?> response = registrationController.getRPRegistrationConfig(CLIENT_ID, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.FOUND);
        assertFalse(Utils.isNullOrEmpty(response.getHeaders().get("Set-Cookie")));
        assertFalse(Utils.isNullOrEmpty(response.getHeaders().get("Location")));
        assertEquals(response.getHeaders().get("Set-Cookie").size(), NO_OF_COOKIES);
    }

    @Test
    public void getRPRegistrationConfig_GivenMethodCalled_WhenParametersAreValidAndURiDoesNotExistInClientURIs_ThenShouldReturnBadRequest() throws Exception {
        // given
        String redirectUtl = "http://example3.com";
        List<String> redirectUris = new ArrayList<>(Arrays.asList("http://example.com", "http://example2.com"));
        String description = "Description";
        OpenIdRelyingParty openIdRelyingParty = OpenIdRelyingParty.builder()
            .clientId(CLIENT_ID)
            .description(description)
            .redirectUris(redirectUris)
            .build();
        String params = "?state=state&redirect_uri=redirect";
        when(encodeService.encodeUTF8(anyString())).thenReturn(URLDecoder.decode(params, StandardCharsets.UTF_8.toString()));
        when(gigyaService.getRP(anyString())).thenReturn(openIdRelyingParty);

        // when
        ResponseEntity<?> response = registrationController.getRPRegistrationConfig(CLIENT_ID, redirectUtl, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getRPRegistrationConfig_GivenMethodCalled_WhenParametersAreValidAndClientIdDoesNotExists_ThenShouldReturnBadRequest() throws Exception {
        //given
        String params = "?state=state&redirect_uri=redirect";
        when(encodeService.encodeUTF8(anyString())).thenReturn(URLDecoder.decode(params, StandardCharsets.UTF_8.toString()));
        when(gigyaService.getRP(anyString())).thenThrow(new CustomGigyaErrorException("404000"));

        // when
        ResponseEntity<?> response = registrationController.getRPRegistrationConfig(CLIENT_ID, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getRPRegistrationConfig_GivenMethodCalled_WhenParametersAreValidAndAErrorOccurred_ThenShouldReturnBadRequest() throws Exception {
        //given
        String params = "?state=state&redirect_uri=redirect";
        when(encodeService.encodeUTF8(anyString())).thenReturn(URLDecoder.decode(params, StandardCharsets.UTF_8.toString()));
        when(gigyaService.getRP(anyString())).thenThrow(new CustomGigyaErrorException("599999"));

        // when
        ResponseEntity<?> response = registrationController.getRPRegistrationConfig(CLIENT_ID, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getRPRegistrationConfig_GivenMethodCalled_WhenClientIDIsNullOrEmpty_ThenShouldReturnBadRequest() throws Exception {
        //given
        String params = "?state=state&redirect_uri=redirect";
        when(encodeService.encodeUTF8(anyString())).thenReturn(URLDecoder.decode(params, StandardCharsets.UTF_8.toString()));

        // when
        ResponseEntity<?> response = registrationController.getRPRegistrationConfig(null, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getRPRegistrationConfig_GivenMethodCalled_WhenRedirectURLIsNullOrEmpty_ThenShouldReturnBadRequest() throws Exception {
        //given
        String params = "?state=state&redirect_uri=redirect";
        when(encodeService.encodeUTF8(anyString())).thenReturn(URLDecoder.decode(params, StandardCharsets.UTF_8.toString()));

        // when
        ResponseEntity<?> response = registrationController.getRPRegistrationConfig(CLIENT_ID, "", STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getRPRegistrationConfig_GivenMethodCalled_WhenGSKeyNotFoundExceptionIsThrown_ThenShouldReturnInternalServerError() throws Exception {
        // given
        ReflectionTestUtils.setField(registrationController, "createAccountEndpointPath", CREATE_ACCOUNT_ENDPOINT_PATH);
        ReflectionTestUtils.setField(registrationController, "getOidcLoginEndpointPath", GET_LOGIN_ENDPOINT_PATH);
        String params = "?state=state&redirect_uri=redirect";
        when(encodeService.encodeUTF8(anyString())).thenReturn(URLDecoder.decode(params, StandardCharsets.UTF_8.toString()));
        when(gigyaService.getRP(anyString())).thenThrow(new GSKeyNotFoundException(""));
        when(cookieService.createCIPAuthDataCookie(any(CIPAuthDataDTO.class), anyString())).thenReturn(RandomStringUtils.randomAlphanumeric(10));

        // when
        ResponseEntity<?> response = registrationController.getRPRegistrationConfig(CLIENT_ID, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void redirectLoginAuth_GivenMethodCalled_WhenCookieExistAndIsValid_ThenShouldReturnURL() throws Exception {
        // given
        cipAuthData = CIPAuthDataDTO.builder()
                .clientId("clientId")
                .redirectUri("redirectUri")
                .responseType("responseType")
                .scope("scope")
                .state("state")
                .build();
        String params = "?state=state&redirect_uri=redirect";
        String queryParams = "https://www.thermofisher.com?client_id=clientId&redirect_uri=redirectUri&state=state&scope=scope&response_type=responseType";
        when(cookieService.decodeCIPAuthDataCookie(COOKIE_CIP_AUTHDATA_VALID)).thenReturn(cipAuthData);
        when(urlService.queryParamMapper(cipAuthData)).thenReturn(queryParams);
        when(encodeService.encodeUTF8(anyString())).thenReturn(URLDecoder.decode(params, StandardCharsets.UTF_8.toString()));

        // when
        ResponseEntity<?> response = registrationController.redirectLoginAuth(COOKIE_CIP_AUTHDATA_VALID, REDIRECT_URL, IS_SIGN_IN_URL);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void redirectLoginAuth_GivenMethodCalled_WhenCookieExistAndIsInvalid_ThenShouldReturnBadRequestCode() throws UnsupportedEncodingException {
        // given
        cipAuthData = CIPAuthDataDTO.builder()
                .redirectUri("redirectUri")
                .responseType("responseType")
                .scope("scope")
                .state("state")
                .build();
        when(cookieService.decodeCIPAuthDataCookie(COOKIE_CIP_AUTHDATA_INVALID)).thenReturn(cipAuthData);

        // when
        ResponseEntity<?> response = registrationController.redirectLoginAuth(COOKIE_CIP_AUTHDATA_INVALID, REDIRECT_URL, IS_SIGN_IN_URL);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void redirectLoginAuth_GivenMethodCalled_WhenCookieDoesntExistAndRedirectURLExist_ThenShouldReturnRedirectURL() throws UnsupportedEncodingException {
        // given
        String cookie = null;
        cipAuthData = null;
        when(cookieService.decodeCIPAuthDataCookie(COOKIE_CIP_AUTHDATA_INVALID)).thenReturn(cipAuthData);
        when(loginService.generateDefaultLoginUrl("https://www.thermofisher.com/")).thenReturn("https://www.dev3.thermofisher.com/auth/login?returnUrl=http://example.com");

        // when
        ResponseEntity<?> response = registrationController.redirectLoginAuth(cookie, REDIRECT_URL, IS_SIGN_IN_URL);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void redirectLoginAuth_GivenMethodCalled_WhenCookieAndRedirectURLDoesntExist_ThenShouldReturnDefaultSignInRedirectUrl() throws UnsupportedEncodingException {
        // given
        String cookie = null;
        String redirectUrl = null;
        String params = "?state=state&redirect_uri=redirect";
        when(encodeService.encodeUTF8(anyString())).thenReturn(URLDecoder.decode(params, StandardCharsets.UTF_8.toString()));
        
        // when
        ResponseEntity<?> response = registrationController.redirectLoginAuth(cookie, redirectUrl, IS_SIGN_IN_URL);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void redirectLoginAuth_GivenMethodCalled_WhenCipAuthDataDoesntExistsAndRedirectExistsAndIsNotSignInUrl_ThenShouldReturnAnOkHttpStatusCode() throws UnsupportedEncodingException {
        // given
        String cookie = "";
        String redirectUrl = "http://google.com";
        String params = "?state=state&redirect_uri=redirect";
        when(encodeService.encodeUTF8(anyString())).thenReturn(URLDecoder.decode(params, StandardCharsets.UTF_8.toString()));

        // when
        ResponseEntity<?> response = registrationController.redirectLoginAuth(cookie, redirectUrl, false);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void redirectLoginAuth_GivenMethodCalled_WhenCookieClientIdIsInTFAndIsNotSignInUrl_ThenShouldReturnAnOkHttpStatusCode() throws UnsupportedEncodingException {
        // given
        ReflectionTestUtils.setField(registrationController, "tfComClientId", "tfComClientId");
        cipAuthData = CIPAuthDataDTO.builder()
                .clientId("tfComClientId")
                .redirectUri("redirectUri")
                .responseType("responseType")
                .scope("scope")
                .state("state")
                .build();
        String params = "?state=state&redirect_uri=redirect";
        String queryParams = "https://www.thermofisher.com?client_id=clientId&redirect_uri=redirectUri&state=state&scope=scope&response_type=responseType";
        when(cookieService.decodeCIPAuthDataCookie(COOKIE_CIP_AUTHDATA_VALID)).thenReturn(cipAuthData);
        when(urlService.queryParamMapper(cipAuthData)).thenReturn(queryParams);
        when(encodeService.encodeUTF8(anyString())).thenReturn(URLDecoder.decode(params, StandardCharsets.UTF_8.toString()));

        // when
        ResponseEntity<?> response = registrationController.redirectLoginAuth(COOKIE_CIP_AUTHDATA_VALID, REDIRECT_URL, false);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void redirectLoginAuth_GivenMethodCalled_WhenJsonParseExceptionIsThrown_ThenShouldReturnBadRequestHttpStatusCode() throws UnsupportedEncodingException {
        // given
        ReflectionTestUtils.setField(registrationController, "tfComClientId", "tfComClientId");
        cipAuthData = CIPAuthDataDTO.builder()
                .clientId("tfComClientId")
                .redirectUri("redirectUri")
                .responseType("responseType")
                .scope("scope")
                .state("state")
                .build();
        String params = "?state=state&redirect_uri=redirect";
        String queryParams = "https://www.thermofisher.com?client_id=clientId&redirect_uri=redirectUri&state=state&scope=scope&response_type=responseType";
        when(cookieService.decodeCIPAuthDataCookie(COOKIE_CIP_AUTHDATA_VALID)).thenThrow(new JsonParseException(""));
        when(urlService.queryParamMapper(cipAuthData)).thenReturn(queryParams);
        when(encodeService.encodeUTF8(anyString())).thenReturn(URLDecoder.decode(params, StandardCharsets.UTF_8.toString()));

        // when
        ResponseEntity<?> response = registrationController.redirectLoginAuth(COOKIE_CIP_AUTHDATA_VALID, REDIRECT_URL, false);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void redirectLoginAuth_GivenMethodCalled_WhenNullPointerExceptionIsThrown_ThenShouldReturnInternalServerErrorHttpStatusCode() throws UnsupportedEncodingException {
        // given
        ReflectionTestUtils.setField(registrationController, "tfComClientId", "tfComClientId");
        cipAuthData = CIPAuthDataDTO.builder()
                .clientId("tfComClientId")
                .redirectUri("redirectUri")
                .responseType("responseType")
                .scope("scope")
                .state("state")
                .build();
        String params = "?state=state&redirect_uri=redirect";
        String queryParams = "https://www.thermofisher.com?client_id=clientId&redirect_uri=redirectUri&state=state&scope=scope&response_type=responseType";
        when(cookieService.decodeCIPAuthDataCookie(COOKIE_CIP_AUTHDATA_VALID)).thenThrow(new NullPointerException(""));
        when(urlService.queryParamMapper(cipAuthData)).thenReturn(queryParams);
        when(encodeService.encodeUTF8(anyString())).thenReturn(URLDecoder.decode(params, StandardCharsets.UTF_8.toString()));

        // when
        ResponseEntity<?> response = registrationController.redirectLoginAuth(COOKIE_CIP_AUTHDATA_VALID, REDIRECT_URL, false);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    // given the cip_authdata cookie has the custom rp properties, it should return the signInRedirectUri property
    public void returnSignInRedirectUri() throws UnsupportedEncodingException {
        // given
        String signInRedirectUri = "https://www.thermofisher.com";
        cipAuthData = CIPAuthDataDTO.builder()
            .clientId("clientId")
            .regRedirectUri("regRedirectUri")
            .signInRedirectUri(signInRedirectUri)
            .returnUrl("returnUrl")
            .build();
        when(cookieService.decodeCIPAuthDataCookie(CUSTOM_RP_CIP_AUTHDATA_COOKIE)).thenReturn(cipAuthData);

        // when
        ResponseEntity<?> response = registrationController.redirectLoginAuth(CUSTOM_RP_CIP_AUTHDATA_COOKIE, REDIRECT_URL, IS_SIGN_IN_URL);

        // then
        assertEquals(signInRedirectUri, response.getBody());
    }
}
