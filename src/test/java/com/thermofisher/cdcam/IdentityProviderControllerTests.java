package com.thermofisher.cdcam;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.controller.IdentityProviderController;
import com.thermofisher.cdcam.model.identityProvider.IdentityProviderResponse;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import com.thermofisher.cdcam.utils.IdentityProviderUtils;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class IdentityProviderControllerTests {
    @InjectMocks
    IdentityProviderController identityProviderController;

    @Mock
    CDCResponseHandler cdcResponseHandler;

    @Test
    public void getIdentityProviderInformation_ShouldReturn_OK_IfTheIdPGetsFound() {
        // given
        final String IDP_NAME = "FID-NOVARTIS";
        IdentityProviderResponse mockResponse = IdentityProviderUtils.buildTestResponse();
        when(cdcResponseHandler.getIdPInformation(any(String.class))).thenReturn(mockResponse);
        ReflectionTestUtils.setField(identityProviderController, "cdcResponseHandler", cdcResponseHandler);
        
        // when
        ResponseEntity<IdentityProviderResponse> response = identityProviderController.getIdentityProviderInformation(IDP_NAME); 

        // then
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getIdentityProviderInformation_ShouldReturn_BAD_REQUEST_IfTheIdPIsNotFound() {
        // given
        final String IDP_NAME = "TEST";
        when(cdcResponseHandler.getIdPInformation(any(String.class))).thenReturn(null);
       
        // when
        ResponseEntity<IdentityProviderResponse> response = identityProviderController.getIdentityProviderInformation(IDP_NAME); 

        // then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
