package com.thermofisher.cdcam.builders;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.gigya.socialize.GSObject;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.model.identityProvider.IdentityProviderResponse;
import com.thermofisher.cdcam.utils.IdentityProviderUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
//@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest//(classes = CdcamApplication.class)
public class IdentityProviderBuilderTests {
    @InjectMocks
    private IdentityProviderBuilder identityProviderBuilder;
    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    public void getIdPInformation_ShouldBuiltAnIdentityProviderResponseWithTheIdPInformation() throws Exception {
        // given
        String data = IdentityProviderUtils.getIdentityProviderJsonString();
        GSObject gsObject = new GSObject(data);

        // when
        IdentityProviderResponse result = identityProviderBuilder.getIdPInformation(gsObject);
        
        // then
        assertNotNull(result);
        assertNotNull(result.getName());
    }

    @Test
    public void getIdPInformation_ShouldNotBuiltAnIdentityProviderResponseAndReturnNull() throws Exception {
        // given
        final String EMPTY_JSON = "{\n" + "}";
        GSObject gsObject = new GSObject(EMPTY_JSON);

        // when
        IdentityProviderResponse result = identityProviderBuilder.getIdPInformation(gsObject);

        // then
        assertNull(result);
    }
}
