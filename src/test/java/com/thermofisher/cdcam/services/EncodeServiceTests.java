package com.thermofisher.cdcam.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class EncodeServiceTests {
    private final String NORMAL_TEXT = "{\"clientId\":\"clientId\",\"redirectUri\":\"redirectUri\",\"state\":\"state\",\"scope\":\"scope\",\"responseType\":\"responseType\"}";
    private final byte[] BASE_64 = new String("eyJjbGllbnRJZCI6ImNsaWVudElkIiwicmVkaXJlY3RVcmkiOiJyZWRpcmVjdFVyaSIsInN0YXRlIjoic3RhdGUiLCJzY29wZSI6InNjb3BlIiwicmVzcG9uc2VUeXBlIjoicmVzcG9uc2VUeXBlIn0=").getBytes();

    @InjectMocks
    EncodeService encodeService;
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
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

    @Test
    public void encodeUTF8_givenAText_whenMethodIsCalled_thenReturnAValidUTF8Text() throws UnsupportedEncodingException {
        // given
        String text = "http://www.test.com/?test=30&message=\"hello world\"";
        String result = "http%3A%2F%2Fwww.test.com%2F%3Ftest%3D30%26message%3D%22hello+world%22";
        // when
        String base64Text = encodeService.encodeUTF8(text);
        // then
        assertTrue(base64Text.equals(result));
    }

    @Test
    public void decodeUTF8_givenAText_whenMethodIsCalled_thenReturnAValidText() throws UnsupportedEncodingException {
        // given
        String text = "http%3A%2F%2Fwww.test.com%2F%3Ftest%3D30%26message%3D%22hello+world%22";
        String result = "http://www.test.com/?test=30&message=\"hello world\"";
        // when
        String base64Text = encodeService.decodeUTF8(text);
        // then
        assertTrue(base64Text.equals(result));
    }
}
