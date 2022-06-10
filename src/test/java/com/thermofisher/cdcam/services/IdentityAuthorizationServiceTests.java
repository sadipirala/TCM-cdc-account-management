package com.thermofisher.cdcam.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.thermofisher.CdcamApplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class IdentityAuthorizationServiceTests {

    @InjectMocks
    IdentityAuthorizationService identityAuthorizationService;

    @Test
    public void encodeRedirectAuthUrl_givenAString_whenMethodIsCalled_thenReturnAnEncodedString() {
        // given}
        String redirectUrl = "http://example.com";
        String mockString = "https://www.dev3.thermofisher.com/api-gateway/identity-authorization/identity/oidc/op/authorize?response_type=code&client_id=eZc3CGSFO2-phATVvTvL_4tf&redirect_uri=https://www.dev3.thermofisher.com/api-gateway/identity-authorization/identity/auth/token&scope=openid%20profile%20email&state=%7B%22u%22%3A%22https%3A%2F%2Fwww.dev3.thermofisher.com%2Forder%2Fcatalog%2Fen%2FUS%2Fadirect%2Flt%3Fcmd%3DpartnerMktLogin%26newAccount%3Dtrue%26LoginData-referer%3Dtrue%26LoginData-ReturnURL%3Dhttp%3A%2F%2Fexample.com%22%7D";
        ReflectionTestUtils.setField(identityAuthorizationService,"authorize","https://www.dev3.thermofisher.com/api-gateway/identity-authorization/identity/oidc/op/authorize?response_type=code");
        ReflectionTestUtils.setField(identityAuthorizationService,"clientId","&client_id=eZc3CGSFO2-phATVvTvL_4tf");
        ReflectionTestUtils.setField(identityAuthorizationService,"identityRedirectUri","&redirect_uri=https://www.dev3.thermofisher.com/api-gateway/identity-authorization/identity/auth/token");
        ReflectionTestUtils.setField(identityAuthorizationService,"scope","&scope=openid%20profile%20email");
        ReflectionTestUtils.setField(identityAuthorizationService,"state","&state=");
        ReflectionTestUtils.setField(identityAuthorizationService,"u","https://www.dev3.thermofisher.com/order/catalog/en/US/adirect/lt?cmd=partnerMktLogin&newAccount=true&LoginData-referer=true&LoginData-ReturnURL=");

        // when
        String expectedGeneratedString = identityAuthorizationService.generateRedirectAuthUrl(redirectUrl);

        // then
        assertEquals(mockString, expectedGeneratedString);
    }

    @Test
    public void encodeRedirectAuthUrl_givenAString_whenMethodIsCalled_thenShouldThrowAnException() {
        // given
        String redirectUrl = "http://example.com";
        String mockString = "https://www.dev3.thermofisher.com/api-gateway/identity-authorization/identity/oidc/op/authorize?response_type=code&client_id=eZc3CGSFO2-phATVvTvL_4tf&redirect_uri=https://www.dev3.thermofisher.com/api-gateway/identity-authorization/identity/auth/token&scope=openid%20profile%20email&state=%7B%22u%22%3A%22https%3A%2F%2Fwww.dev3.thermofisher.com%2Forder%2Fcatalog%2Fen%2FUS%2Fadirect%2Flt%3Fcmd%3DpartnerMktLogin%26newAccount%3Dtrue%26LoginData-referer%3Dtrue%26LoginData-ReturnURL%3Dhttp%3A%2F%2Fexample.com%22%7D";
        ReflectionTestUtils.setField(identityAuthorizationService,"authorize","https://www.dev3.thermofisher.com/api-gateway/identity-authorization/identity/oidc/op/authorize?response_type=code");
        ReflectionTestUtils.setField(identityAuthorizationService,"clientId","&client_id=eZc3CGSFO2-phATVvTvL_4tf");
        ReflectionTestUtils.setField(identityAuthorizationService,"identityRedirectUri","&redirect_uri=https://www.dev3.thermofisher.com/api-gateway/identity-authorization/identity/auth/token");
        ReflectionTestUtils.setField(identityAuthorizationService,"scope","&scope=openid%20profile%20email");
        ReflectionTestUtils.setField(identityAuthorizationService,"state","&state=");
        ReflectionTestUtils.setField(identityAuthorizationService,"u","https://www.dev3.thermofisher.com/order/catalog/en/US/adirect/lt?cmd=partnerMktLogin&newAccount=true&LoginData-referer=true&LoginData-ReturnURL=");

        try (MockedStatic<URLEncoder> urlEncoder = Mockito.mockStatic(URLEncoder.class)) {
            urlEncoder.when(() -> URLEncoder.encode(anyString(), anyString())).thenThrow(UnsupportedEncodingException.class);

            // when
            String expectedGeneratedString = identityAuthorizationService.generateRedirectAuthUrl(redirectUrl);

            // then
            assertNotEquals(mockString, expectedGeneratedString);
        }
    }

    @Test
    public void generateDefaultRedirectSignInUrl_whenMethodIsCalled_thenReturnAnEncodedString() {
        // given
        String expectedString = "https://www.dev3.thermofisher.com/api-gateway/identity-authorization/identity/oidc/op/authorize?response_type=code&client_id=eZc3CGSFO2-phATVvTvL_4tf&redirect_uri=https://www.dev3.thermofisher.com/api-gateway/identity-authorization/identity/auth/token&scope=openid%20profile%20email&state=";
        ReflectionTestUtils.setField(identityAuthorizationService,"authorize","https://www.dev3.thermofisher.com/api-gateway/identity-authorization/identity/oidc/op/authorize?response_type=code");
        ReflectionTestUtils.setField(identityAuthorizationService,"clientId","&client_id=eZc3CGSFO2-phATVvTvL_4tf");
        ReflectionTestUtils.setField(identityAuthorizationService,"identityRedirectUri","&redirect_uri=https://www.dev3.thermofisher.com/api-gateway/identity-authorization/identity/auth/token");
        ReflectionTestUtils.setField(identityAuthorizationService,"scope","&scope=openid%20profile%20email");
        ReflectionTestUtils.setField(identityAuthorizationService,"state","&state=");
        ReflectionTestUtils.setField(identityAuthorizationService,"u","https://www.dev3.thermofisher.com/order/catalog/en/US/adirect/lt?cmd=partnerMktLogin&newAccount=true&LoginData-referer=true&LoginData-ReturnURL=");

        // when
        String expectedGeneratedString = identityAuthorizationService.generateDefaultRedirectSignInUrl();

        // then
        assertEquals(expectedString, expectedGeneratedString);
    }

    @Test
    public void buildDefaultStateProperty_whenMethodIsCalled_thenShouldBuildDefaultStateProperty() {
        // given
        String redirectUrl = "http://test.com";
        String mockResult = "{\"u\":\"https://www.dev3.thermofisher.com/order/catalog/en/US/adirect/lt?cmd=partnerMktLogin&newAccount=true&LoginData-referer=true&LoginData-ReturnURL=http://test.com\"}";
        ReflectionTestUtils.setField(identityAuthorizationService,"u","https://www.dev3.thermofisher.com/order/catalog/en/US/adirect/lt?cmd=partnerMktLogin&newAccount=true&LoginData-referer=true&LoginData-ReturnURL=");
        // when
        String expectedGeneratedString = identityAuthorizationService.buildDefaultStateProperty(redirectUrl);

        // then
        assertEquals(mockResult, expectedGeneratedString);
    }
}
