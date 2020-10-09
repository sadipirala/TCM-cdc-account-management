package com.thermofisher.cdcam.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.model.HttpServiceResponse;
import com.thermofisher.cdcam.model.UsernameRecoveryEmailRequest;
import com.thermofisher.cdcam.utils.EmailRequestBuilderUtils;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class EmailServiceTest {

  @InjectMocks
  EmailService emailService;

  @Mock
  HttpService httpService;

  @Test
  public void sendUsernameRecoveryEmail_ShouldMakeAPostRequestToTheEmailNotificationUrl() throws IOException {
    String username = "armadillo-dillo";
    StatusLine mockStatusLine = mock(StatusLine.class);
    CloseableHttpResponse mockHttpCloseableResponse = mock(CloseableHttpResponse.class);
    HttpServiceResponse mockHttpResponse = HttpServiceResponse.builder()
            .closeableHttpResponse(mockHttpCloseableResponse)
            .build();
    when(mockStatusLine.getStatusCode()).thenReturn(200);
    when(mockHttpResponse.getCloseableHttpResponse().getStatusLine()).thenReturn(mockStatusLine);
    when(httpService.post(any(), any())).thenReturn(mockHttpResponse);
    UsernameRecoveryEmailRequest request = EmailRequestBuilderUtils.buildUsernameRecoveryEmailRequest(username);
    
    // when
    emailService.sendUsernameRecoveryEmail(request);

    // then
    verify(httpService).post(any(), any());
  }
}
