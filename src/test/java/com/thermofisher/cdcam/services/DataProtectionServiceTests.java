package com.thermofisher.cdcam.services;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.model.HttpServiceResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ActiveProfiles("test")
@SpringBootTest(classes = CdcamApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DataProtectionServiceTests {

    @InjectMocks
    DataProtectionService dataProtectionService;

    @Mock
    HttpService httpService;

    @Test
    public void decrypt_givenCipherTextIsProvided_ThenDecryptionCallShouldBeMade() throws JSONException, UnsupportedEncodingException {
        // given
        JSONObject decryptResponse = new JSONObject();
        decryptResponse.put("firstName", "John");
        decryptResponse.put("lastName", "Doe");
        decryptResponse.put("email", "john.doe@thermofisher.com");
        HttpServiceResponse httpResponse = HttpServiceResponse.builder().responseBody(decryptResponse).build();

        //when
        when(httpService.get(any())).thenReturn(httpResponse);

        //then
        String encryptedData = "VTJGc2RHVmtYMThPaXlrKzMrVHh4V2E3c1loNGhNZUJXNHowT2R6T3k1QzFmZVQxQkxMcmlNUmgxbTcxWGJYWEVPUUQ5K0doNWhUbjdqVmZ2NGZXbFJnR3BWUWJFWTd0WjNCejcxZVhBQURVS21XUm03b0t5TWZDSDkxdnBJSjE=";
        JSONObject response = dataProtectionService.decrypt(encryptedData);
        assertEquals(decryptResponse, response);
    }
}
