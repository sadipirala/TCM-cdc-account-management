package com.thermofisher.cdcam;

import com.thermofisher.cdcam.controller.IdentityProviderController;
import com.thermofisher.cdcam.model.identityProvider.IdentityProviderResponse;
import com.thermofisher.cdcam.services.GigyaService;
import com.thermofisher.cdcam.utils.IdentityProviderUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IdentityProviderControllerTests {

    @InjectMocks
    IdentityProviderController identityProviderController;

    @Mock
    GigyaService gigyaService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getIdentityProviderInformation_ShouldReturn_OK_IfTheIdPGetsFound() {
        // given
        final String IDP_NAME = "FID-NOVARTIS";
        IdentityProviderResponse mockResponse = IdentityProviderUtils.buildTestResponse();
        when(gigyaService.getIdPInformation(any(String.class))).thenReturn(mockResponse);
        ReflectionTestUtils.setField(identityProviderController, "gigyaService", gigyaService);

        // when
        ResponseEntity<IdentityProviderResponse> response = identityProviderController.getIdentityProviderInformation(IDP_NAME);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getIdentityProviderInformation_ShouldReturn_BAD_REQUEST_IfTheIdPIsNotFound() {
        // given
        final String IDP_NAME = "TEST";
        when(gigyaService.getIdPInformation(any(String.class))).thenReturn(null);

        // when
        ResponseEntity<IdentityProviderResponse> response = identityProviderController.getIdentityProviderInformation(IDP_NAME);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
