package com.thermofisher.cdcam.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;

import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.model.*;
import com.thermofisher.cdcam.model.dto.RequestResetPasswordDTO;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.EmailRequestBuilderUtils;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class EmailServiceTest {
  private AccountInfo accountInfo = AccountUtils.getSiteAccount();
  private RequestResetPasswordDTO requestResetPasswordDTO = RequestResetPasswordDTO.builder()
          .passwordToken("passwordToken")
          .authData("authData")
          .build();
  private String resetPasswordUrl = "resetPasswordUrl";
  private EmailUserInfo emailUserInfo = EmailUserInfo.builder()
          .email("email")
          .redirectUrl("redirectUrl")
          .lastName("lastName")
          .firstName("firstName")
          .build();
  private EmailRequestResetPassword emailRequestResetPassword = EmailRequestResetPassword.builder()
          .userInfo(emailUserInfo)
          .resetPasswordUrl(resetPasswordUrl)
          .build();

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

  @Test
  public void sendRequestResetPasswordEmail_ShouldMakeAPostRequestToTheEmailNotificationUrl() throws IOException {
    StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
    HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
    CloseableHttpResponse mockHttpCloseableResponse = Mockito.mock(CloseableHttpResponse.class);

    HttpServiceResponse mockHttpResponse = HttpServiceResponse.builder()
            .closeableHttpResponse(mockHttpCloseableResponse)
            .build();

    Mockito.when(mockStatusLine.getStatusCode()).thenReturn(500);
    Mockito.when(mockHttpResponse.getCloseableHttpResponse().getStatusLine()).thenReturn(mockStatusLine);
    Mockito.when(mockHttpResponse.getCloseableHttpResponse().getEntity()).thenReturn(mockEntity);
    Mockito.when(httpService.post(any(), any())).thenReturn(mockHttpResponse);

    emailService.sendRequestResetPasswordEmail(emailRequestResetPassword, accountInfo);

    verify(httpService, times(1)).post(any(), any());
  }
}
