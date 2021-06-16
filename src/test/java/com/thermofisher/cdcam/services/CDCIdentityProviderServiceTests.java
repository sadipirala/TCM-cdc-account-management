package com.thermofisher.cdcam.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.aws.SecretsManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

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
}
