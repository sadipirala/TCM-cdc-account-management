package com.thermofisher.cdcam;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.controller.IdentityProviderController;
import com.thermofisher.cdcam.model.identityProvider.IdentityProviderResponse;
import com.thermofisher.cdcam.services.GigyaService;
import com.thermofisher.cdcam.utils.IdentityProviderUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
//@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class IdentityProviderControllerTests {

    @InjectMocks
    IdentityProviderController identityProviderController;

    @Mock
    GigyaService gigyaService;
    @Before
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
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getIdentityProviderInformation_ShouldReturn_BAD_REQUEST_IfTheIdPIsNotFound() {
        // given
        final String IDP_NAME = "TEST";
        when(gigyaService.getIdPInformation(any(String.class))).thenReturn(null);
       
        // when
        ResponseEntity<IdentityProviderResponse> response = identityProviderController.getIdentityProviderInformation(IDP_NAME); 

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
