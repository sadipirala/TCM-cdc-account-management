package com.thermofisher.cdcam;

import static org.junit.Assert.assertTrue;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.services.EncodeService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class EncodeServiceTests {
    private final String NORMAL_TEXT = "{\"clientId\":\"clientId\",\"redirectUri\":\"redirectUri\",\"state\":\"state\",\"scope\":\"scope\",\"responseType\":\"responseType\"}";
    private final byte[] BASE_64 = new String("eyJjbGllbnRJZCI6ImNsaWVudElkIiwicmVkaXJlY3RVcmkiOiJyZWRpcmVjdFVyaSIsInN0YXRlIjoic3RhdGUiLCJzY29wZSI6InNjb3BlIiwicmVzcG9uc2VUeXBlIjoicmVzcG9uc2VUeXBlIn0=").getBytes();

    @InjectMocks
    EncodeService encodeService;

    @Test
    public void encodeBase64_givenAText_whenMethodIsCalled_thenReturnAValidBase64Text() {
        // when
        String base64 = new String(encodeService.encodeBase64(NORMAL_TEXT));

        // then
        assertTrue(base64.matches("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$"));
    }

    @Test
    public void decodeBase64_givenAText_whenMethodIsCalled_thenReturnAValidBase64Text() {
        // when
        String base64Text = encodeService.decodeBase64(BASE_64);
        // then
        assertTrue(base64Text.equals(NORMAL_TEXT));
    }
}
