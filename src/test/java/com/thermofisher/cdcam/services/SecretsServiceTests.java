package com.thermofisher.cdcam.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.thermofisher.cdcam.aws.SecretsManager;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SecretsService.class, SecretsManager.class })
public class SecretsServiceTests {

    @InjectMocks
    SecretsService secretsService;

    @Mock
    SecretsManager secretsManager;

    @Before
    public void setup() throws JSONException {
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

