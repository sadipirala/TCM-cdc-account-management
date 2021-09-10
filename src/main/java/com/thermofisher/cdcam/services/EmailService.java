package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.EmailRequestResetPassword;
import com.thermofisher.cdcam.model.EmailSentResponse;
import com.thermofisher.cdcam.model.UsernameRecoveryEmailRequest;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {
  private Logger logger = LogManager.getLogger(this.getClass());

  @Value("${tfrn.email-notification.url}")
  private String emailNotificationUrl;

  @Autowired
  HttpService httpService;
  
  public EmailSentResponse sendUsernameRecoveryEmail(UsernameRecoveryEmailRequest usernameRecoveryEmailRequest) {
    int status = 0;
    JSONObject requestBody = new JSONObject(usernameRecoveryEmailRequest);

    try {
      CloseableHttpResponse response = httpService.post(emailNotificationUrl, requestBody).getCloseableHttpResponse();
      status = response.getStatusLine().getStatusCode();
      HttpStatus httpStatus = HttpStatus.valueOf(status);
      if (httpStatus.is2xxSuccessful()) {
        logger.info(String.format("Username recovery email sent to: %s", usernameRecoveryEmailRequest.getUserInfo().getEmail()));
      } else {
        logger.warn(String.format("Something went wrong while sending the username recovery email to: %s. Status: %d", usernameRecoveryEmailRequest.getUserInfo().getEmail(), status));
      }
    } catch(Exception e) {
      logger.error(String.format("Something went wrong while connecting to the email notification service."));
      throw e;
    }

    return EmailSentResponse.builder()
      .statusCode(status)
      .build();
  }

  public void sendRequestResetPasswordEmail(EmailRequestResetPassword emailRequestResetPassword, AccountInfo accountInfo) throws IOException {
    JSONObject requestBody = new JSONObject(emailRequestResetPassword);
    CloseableHttpResponse response = (httpService.post(emailNotificationUrl, requestBody)).getCloseableHttpResponse();
    HttpEntity responseEntity = response.getEntity();

    if (responseEntity != null) {
      int status = response.getStatusLine().getStatusCode();
      HttpStatus httpStatus = HttpStatus.valueOf(status);

      if (httpStatus.is2xxSuccessful()) {
        logger.info(String.format("Request Reset Password confirmation email sent to: %s", accountInfo.getEmailAddress()));
      } else {
        logger.warn(String.format("Something went wrong while sending the Request Reset Password confirmation email to: %s. Status: %d",
                accountInfo.getEmailAddress(), status));
      }

    } else {
      logger.error(String.format("Something went wrong while connecting to the email notification service. UID: %s", accountInfo.getUid()));
      throw new IOException();
    }
  }
}
