package com.thermofisher.cdcam.builders;


import com.gigya.socialize.GSObject;
import com.thermofisher.cdcam.model.identityProvider.IdentityProviderResponse;
import com.thermofisher.cdcam.utils.IdentityProviderUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(MockitoExtension.class)
public class IdentityProviderBuilderTests {
    @InjectMocks
    private IdentityProviderBuilder identityProviderBuilder;
    @BeforeEach
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
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getName()).isNotNull();
    }

    @Test
    public void getIdPInformation_ShouldNotBuiltAnIdentityProviderResponseAndReturnNull() throws Exception {
        // given
        final String EMPTY_JSON = "{\n" + "}";
        GSObject gsObject = new GSObject(EMPTY_JSON);

        // when
        IdentityProviderResponse result = identityProviderBuilder.getIdPInformation(gsObject);

        // then
        Assertions.assertThat(result).isNull();
    }
}
