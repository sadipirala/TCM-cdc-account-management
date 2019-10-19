package com.thermofisher.cdcam;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.services.NotificationService;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class NotificationServiceTests {

    private final String mockRequestUrl = "http://google.com";
    private final String mockRequestBody = "{\"test\":true}";

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private CloseableHttpClient mockHttpClient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void postRequest_givenRequestBody_IsReceived_ReturnCloseableHttpResponse() throws IOException {
        //Given
        CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class);
        StatusLine mockStatusLine = Mockito.mock(StatusLine.class);

        //When
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockHttpClient.execute(any())).thenReturn(mockResponse);
        CloseableHttpResponse httpResponse = notificationService.postRequest(mockRequestBody, mockRequestUrl);

        //Then
        Assert.assertNotNull(httpResponse);
    }

    @Test(expected = IOException.class)
    public void postRequest_givenExecute_HttpClient_Fails_ShouldCatchIOException() throws IOException {
        //When
        when(mockHttpClient.execute(any())).thenThrow(IOException.class);
        notificationService.postRequest(mockRequestBody , mockRequestUrl);
    }
}
