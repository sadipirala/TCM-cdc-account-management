package com.thermofisher.cdcam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.controller.RegistrationController;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.OpenIdRelyingParty;
import com.thermofisher.cdcam.model.dto.CIPAuthDataDTO;
import com.thermofisher.cdcam.services.CookieService;
import com.thermofisher.cdcam.services.EncodeService;
import com.thermofisher.cdcam.services.IdentityAuthorizationService;
import com.thermofisher.cdcam.services.URLService;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;

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

    @InjectMocks
    RegistrationController registrationController;

    @Mock
    CDCResponseHandler cdcResponseHandler;

    @Mock
    CookieService cookieService;

    @Mock
    EncodeService encodeService;

    @Mock
    URLService urlService;

    @Mock
    CIPAuthDataDTO cipAuthData;

    @Mock
    IdentityAuthorizationService identityAuthorizationService;

    @Test
    public void getRPRegistrationConfig_GivenMethodCalled_WhenParametersAreValid_ThenShouldReturnFoundStatusAndCIP_AUTDATAShouldBePresentInHeaders() throws Exception {
        // given
        List<String> redirectUris = new ArrayList<>(Arrays.asList("http://example.com", "http://example2.com"));
        String description = "Description";
        OpenIdRelyingParty openIdRelyingParty = OpenIdRelyingParty.builder()
            .clientId(CLIENT_ID)
            .description(description)
            .redirectUris(redirectUris)
            .build();
        when(cdcResponseHandler.getRP(anyString())).thenReturn(openIdRelyingParty);
        when(cookieService.createCIPAuthDataCookie(any())).thenReturn(anyString());
        // when
        ResponseEntity<?> response = registrationController.getRPRegistrationConfig(CLIENT_ID, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.FOUND );
        assertTrue(!Utils.isNullOrEmpty(response.getHeaders().get("Set-Cookie")));
        assertTrue(!Utils.isNullOrEmpty(response.getHeaders().get("Location")));
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
        when(cdcResponseHandler.getRP(anyString())).thenReturn(openIdRelyingParty);

        // when
        ResponseEntity<?> response = registrationController.getRPRegistrationConfig(CLIENT_ID, redirectUtl, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getRPRegistrationConfig_GivenMethodCalled_WhenParametersAreValidAndClientIdDoesNotExists_ThenShouldReturnNotFound() throws Exception {
        // given
        when(cdcResponseHandler.getRP(anyString())).thenThrow(new CustomGigyaErrorException("404000"));

        // when
        ResponseEntity<?> response = registrationController.getRPRegistrationConfig(CLIENT_ID, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getRPRegistrationConfig_GivenMethodCalled_WhenParametersAreValidAndAErrorOccurred_ThenShouldReturnBadRequest() throws Exception {
        // given
        when(cdcResponseHandler.getRP(anyString())).thenThrow(new CustomGigyaErrorException("599999"));

        // when
        ResponseEntity<?> response = registrationController.getRPRegistrationConfig(CLIENT_ID, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getRPRegistrationConfig_GivenMethodCalled_WhenClientIDIsNullOrEmpty_ThenShouldReturnBadRequest() throws Exception {
        // when
        ResponseEntity<?> response = registrationController.getRPRegistrationConfig(null, REDIRECT_URL, STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getRPRegistrationConfig_GivenMethodCalled_WhenRedirectURLIsNullOrEmpty_ThenShouldReturnInternalServerError() throws Exception {
        // when
        ResponseEntity<?> response = registrationController.getRPRegistrationConfig(CLIENT_ID, "", STATE, RESPONSE_TYPE, SCOPE);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
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
        String queryParams = "https://www.thermofisher.com?client_id=clientId&redirect_uri=redirectUri&state=state&scope=scope&response_type=responseType";
        when(cookieService.decodeCIPAuthDataCookie(COOKIE_CIP_AUTHDATA_VALID)).thenReturn(cipAuthData);
        when(urlService.queryParamMapper(cipAuthData)).thenReturn(queryParams);

        // when
        ResponseEntity<?> response = registrationController.redirectLoginAuth(COOKIE_CIP_AUTHDATA_VALID, REDIRECT_URL);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.FOUND);
    }

    @Test
    public void redirectLoginAuth_GivenMethodCalled_WhenCookieExistAndIsInvalid_ThenShouldReturnBadRequestCode() throws Exception {
        // given
        cipAuthData = CIPAuthDataDTO.builder()
                .redirectUri("redirectUri")
                .responseType("responseType")
                .scope("scope")
                .state("state")
                .build();
        when(cookieService.decodeCIPAuthDataCookie(COOKIE_CIP_AUTHDATA_INVALID)).thenReturn(cipAuthData);
        when(identityAuthorizationService.generateRedirectAuthUrl("https://www.thermofisher.com/")).thenReturn("%7B%22u%22%3A%22https%3A%2F%2Fwww.dev3.thermofisher.com%2Forder%2Fcatalog%2Fen%2FUS%2Fadirect%2Flt%3Fcmd%3DpartnerMktLogin%26newAccount%3Dtrue%26LoginData-referer%3Dtrue%26LoginData-ReturnURL%3D http%3A%2F%2Fthermofisher.com%22%7D");

        // when
        ResponseEntity<?> response = registrationController.redirectLoginAuth(COOKIE_CIP_AUTHDATA_INVALID, REDIRECT_URL);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void redirectLoginAuth_GivenMethodCalled_WhenCookieDoesntExistAndRedirectURLExist_ThenShouldReturnRedirectURL() throws Exception {
        // given
        String cookie = null;
        cipAuthData = null;
        when(cookieService.decodeCIPAuthDataCookie(COOKIE_CIP_AUTHDATA_INVALID)).thenReturn(cipAuthData);
        when(identityAuthorizationService.generateRedirectAuthUrl("https://www.thermofisher.com/")).thenReturn("%7B%22u%22%3A%22https%3A%2F%2Fwww.dev3.thermofisher.com%2Forder%2Fcatalog%2Fen%2FUS%2Fadirect%2Flt%3Fcmd%3DpartnerMktLogin%26newAccount%3Dtrue%26LoginData-referer%3Dtrue%26LoginData-ReturnURL%3D http%3A%2F%2Fthermofisher.com%22%7D");

        // when
        ResponseEntity<?> response = registrationController.redirectLoginAuth(cookie, REDIRECT_URL);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.FOUND);
    }

    @Test
    public void redirectLoginAuth_GivenMethodCalled_WhenCookieAndRedirectURLDoesntExist_ThenShouldReturnBadRequest() throws Exception {
        // given
        String cookie = null;
        String redirectUrl = null;
        
        // when
        ResponseEntity<?> response = registrationController.redirectLoginAuth(cookie, redirectUrl);

        // then
        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
