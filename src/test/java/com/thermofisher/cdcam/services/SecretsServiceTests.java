package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.aws.SecretsManager;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SecretsServiceTests {

    @InjectMocks
    SecretsService secretsService;

    @Mock
    SecretsManager secretsManager;

    @BeforeEach
    public void setup() throws JSONException {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(secretsService, "env", "prod");
        ReflectionTestUtils.setField(secretsService, "cdcamSecretsName", "secret");
        when(secretsManager.getSecret(any())).thenReturn("{\"x\":\"x\"}");
        when(secretsManager.getProperty(any(), anyString())).thenReturn("");
    }

    @Test
    public void get_shouldGetExistingSecret() throws JSONException {
        // given
        final String secret = "top secret";
        final String secretKey = "top secret key";
        when(secretsManager.getProperty(any(), anyString())).thenReturn(secret);

        // when
        String result = secretsService.get(secretKey);

        // then
        assertEquals(secret, result);
    }
}

