package com.thermofisher.cdcam.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.model.Ciphertext;
import com.thermofisher.cdcam.model.HttpServiceResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@SpringBootTest(classes = CdcamApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DataProtectionServiceTests {
    private final String CIPHERTEXT = "VTJGc2RHVmtYMThPaXlrKzMrVHh4V2E3c1loNGhNZUJXNHowT2R6T3k1QzFmZVQxQkxMcmlNUmgxbTcxWGJYWEVPUUQ5K0doNWhUbjdqVmZ2NGZXbFJnR3BWUWJFWTd0WjNCejcxZVhBQURVS21XUm03b0t5TWZDSDkxdnBJSjE=";
    JSONObject ciphertextBody = new JSONObject();

    @InjectMocks
    DataProtectionService dataProtectionService;

    @Mock
    HttpService httpService;

    @Before
    public void beforeEach() throws JSONException {
        ciphertextBody.put("firstName", "John");
        ciphertextBody.put("lastName", "Doe");
        ciphertextBody.put("email", "john.doe@thermofisher.com");
        ciphertextBody.put("source", "IAC");
    }

    @Test
    public void decrypt_givenCipherTextIsProvided_ThenDecryptionCallShouldBeMade() throws JSONException, UnsupportedEncodingException {
        // given
        HttpServiceResponse httpResponse = HttpServiceResponse.builder().responseBody(ciphertextBody).build();
        when(httpService.get(any())).thenReturn(httpResponse);

        //when
        JSONObject response = dataProtectionService.decrypt(CIPHERTEXT);

        //then
        assertEquals(ciphertextBody, response);
    }

    @Test
    public void decrypCiphertext_ShouldDecryptAndReturnTheCiphertext() throws JSONException, UnsupportedEncodingException {
        // given
        JSONObject jsonCiphertext = new JSONObject();
        jsonCiphertext.put("body", ciphertextBody);
        HttpServiceResponse httpResponse = HttpServiceResponse.builder().responseBody(jsonCiphertext).build();
        when(httpService.get(any())).thenReturn(httpResponse);

        // when
        Ciphertext ciphertext = dataProtectionService.decrypCiphertext(CIPHERTEXT);

        // then
        assertEquals(ciphertextBody.get("firstName"), ciphertext.getFirstName());
        assertEquals(ciphertextBody.get("lastName"), ciphertext.getLastName());
        assertEquals(ciphertextBody.get("email"), ciphertext.getEmail());
        assertEquals(ciphertextBody.get("source"), ciphertext.getSource());
    }
}
