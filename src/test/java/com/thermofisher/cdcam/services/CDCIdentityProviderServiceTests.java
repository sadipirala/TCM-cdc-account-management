package com.thermofisher.cdcam.services;

import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CDCIdentityProviderServiceTests {

    @InjectMocks
    CDCIdentityProviderService cdcIdentityProviderService;

    @Mock
    SecretsService secretsService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getIdPInformation_ShouldNotBeReturnedIfTheIdPDoesNotExist() {
        // given
        final String IDP_NAME = "FID-NOVARTIS";
        GSResponse mockGSResponse = Mockito.mock(GSResponse.class);
        GSRequest mockGSRequest = Mockito.mock(GSRequest.class);
//        when(mockGSRequest.send()).thenReturn(mockGSResponse);

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
