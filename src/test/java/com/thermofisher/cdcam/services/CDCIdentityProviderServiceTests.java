package com.thermofisher.cdcam.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.aws.SecretsManager;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { 
    CDCIdentityProviderService.class,
    SecretsService.class,
    SecretsManager.class 
})
public class CDCIdentityProviderServiceTests {

    @InjectMocks
    CDCIdentityProviderService cdcIdentityProviderService;

    @Mock
    SecretsService secretsService;

    @Test
    public void getIdPInformation_ShouldNotBeReturnedIfTheIdPDoesNotExist() {
        // given
        final String IDP_NAME = "FID-NOVARTIS";
        GSResponse mockGSResponse = Mockito.mock(GSResponse.class);
        GSRequest mockGSRequest = Mockito.mock(GSRequest.class);
        when(mockGSRequest.send()).thenReturn(mockGSResponse);

        // when
        GSResponse result = cdcIdentityProviderService.getIdPInformation(IDP_NAME);

        // then
        assertTrue(result.getErrorCode() != 0);
    }

    @Test
    public void setCredentials_shouldSetCredentialsForMainDataCenter() throws JSONException {
        // given
        ReflectionTestUtils.setField(cdcIdentityProviderService, "env", "dev");

        // when
        cdcIdentityProviderService.setCredentials();

        // then
        verify(secretsService).get(any());
    }

    @Test
    public void setCredentials_shouldNotSetCredentials_whenEnvIsTest() throws JSONException {
        // given
        ReflectionTestUtils.setField(cdcIdentityProviderService, "env", "test");

        // when
        cdcIdentityProviderService.setCredentials();

        // then
        verify(secretsService, times(0)).get(any());
    }

    @Test
    public void setCredentials_shouldNotSetCredentials_whenEnvIsLocal() throws JSONException {
        // given
        ReflectionTestUtils.setField(cdcIdentityProviderService, "env", "local");

        // when
        cdcIdentityProviderService.setCredentials();

        // then
        verify(secretsService, times(0)).get(any());
    }

    @Test
    public void setCredentials_shouldThrowASONException() throws JSONException {
        // given
        ReflectionTestUtils.setField(cdcIdentityProviderService, "env", "dev");
        when(secretsService.get(any())).thenThrow(JSONException.class);

        // when
        cdcIdentityProviderService.setCredentials();

        // then
        verify(secretsService, times(1)).get(any());
    }
}
