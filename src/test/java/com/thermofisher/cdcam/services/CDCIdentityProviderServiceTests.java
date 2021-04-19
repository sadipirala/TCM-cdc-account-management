package com.thermofisher.cdcam.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.thermofisher.CdcamApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class CDCIdentityProviderServiceTests {
    @InjectMocks
    CDCIdentityProviderService cdcIdentityProviderService;

    @Test
    public void getIdPInformation_ShouldNotBeReturnedIfTheIdPDoesNotExist() {
        // given
        final String IDP_NAME = "FID-NOVARTIS";
        GSResponse mockGSResponse = Mockito.mock(GSResponse.class);
        GSRequest mockGSRequest = Mockito.mock(GSRequest.class);

        ReflectionTestUtils.setField(cdcIdentityProviderService, "apiKey", "testApiKey");
        ReflectionTestUtils.setField(cdcIdentityProviderService, "secretKey", "testSecretKey");
        ReflectionTestUtils.setField(cdcIdentityProviderService, "userKey", "testUserKey");
        when(mockGSRequest.send()).thenReturn(mockGSResponse);

        // when
        GSResponse result = cdcIdentityProviderService.getIdPInformation(IDP_NAME);

        // then
        assertTrue(result.getErrorCode() != 0);
    }
}
