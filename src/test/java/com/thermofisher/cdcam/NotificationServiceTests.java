package com.thermofisher.cdcam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.services.NotificationService;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class NotificationServiceTests {

    @InjectMocks
    private NotificationService notificationService = new NotificationService();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void postRequest_givenRequestBody_IsReceived_Return_RequestResponse() throws IOException {
        //Given
        StringEntity mockRequestBody = new StringEntity("{\"cdcPreRegistered\":true,\"city\":\"string\",\"company\":\"string\",\"countryCode\":\"string\",\"departmentOrLab\":\"string\",\"emailAddress\":\"test\",\"firstName\":\"string\",\"lastName\":\"string\",\"username\":\"string\",\"uuid\":\"string\"}");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost postMethod = new HttpPost("http://tfgns.qa4.cloudqa.thermofisher.net/api/notification/user/registration");
        postMethod.setEntity(mockRequestBody);
        postMethod.setHeader("Content-type", "application/json");

        //When
        CloseableHttpResponse response = httpClient.execute(postMethod);

        //Then
        Assert.assertThat(response.getStatusLine().getStatusCode(),equalTo(HttpStatus.SC_OK));
    }

    @Test
    public void postRequest_givenRequestBody_IsValid_Return_StatusCode200() throws IOException {
        //Given
        StringEntity mockRequestBody = new StringEntity("{\"cdcPreRegistered\":true,\"city\":\"string\",\"company\":\"string\",\"countryCode\":\"string\",\"departmentOrLab\":\"string\",\"emailAddress\":\"test\",\"firstName\":\"string\",\"lastName\":\"string\",\"username\":\"string\",\"uuid\":\"string\"}");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost postMethod = new HttpPost("http://tfgns.qa4.cloudqa.thermofisher.net/api/notification/user/registration");
        postMethod.setEntity(mockRequestBody);
        postMethod.setHeader("Content-type", "application/json");

        //When
        CloseableHttpResponse response = httpClient.execute(postMethod);

        //Then
        Assert.assertThat(response.getStatusLine().getStatusCode(),equalTo(HttpStatus.SC_OK));
    }

    public void postRequest_givenRequestBody_IsNotValid_Return_StatusCode400() throws IOException {
        //Given
        StringEntity requestBody = new StringEntity("test");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost postMethod = new HttpPost("http://tfgns.qa4.cloudqa.thermofisher.net/api/notification/user/registration");
        postMethod.setEntity(requestBody);
        postMethod.setHeader("Content-type", "application/json");

        //When
        CloseableHttpResponse response = httpClient.execute(postMethod);

        //Then
        Assert.assertThat(response.getStatusLine().getStatusCode(),equalTo(HttpStatus.SC_BAD_REQUEST));
    }

    public void postRequest_givenAnInternalServerErrorOccurs__Return_StatusCode500() throws IOException {
        //Given
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost postMethod = new HttpPost("http://tfgns.qa4.cloudqa.thermofisher.net/api/notification/user/registration");
        StringEntity requestBody = new StringEntity("{\"cdcPreRegistered\":true,\"city\":\"string\",\"company\":\"string\",\"countryCode\":\"string\",\"departmentOrLab\":\"string\",\"emailAddress\":\"test\",\"firstName\":\"string\",\"lastName\":\"string\",\"username\":\"string\",\"uuid\":\"string\"}");
        postMethod.setEntity(requestBody);
        postMethod.setHeader("Content-type", "application/json");

        //When
        CloseableHttpResponse response = httpClient.execute(postMethod);

        //Then
        Assert.assertThat(response.getStatusLine().getStatusCode(),equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR));
    }

    @Test
    public void postRequest_givenRequestWithNoAcceptHeader_whenRequestIsExecuted_thenDefaultResponseContentTypeIsJson() throws IOException {
        //Given
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost postMethod = new HttpPost("http://tfgns.qa4.cloudqa.thermofisher.net/api/notification/user/registration");
        StringEntity requestBody = new StringEntity("{\"cdcPreRegistered\":true,\"city\":\"string\",\"company\":\"string\",\"countryCode\":\"string\",\"departmentOrLab\":\"string\",\"emailAddress\":\"test\",\"firstName\":\"string\",\"lastName\":\"string\",\"username\":\"string\",\"uuid\":\"string\"}");
        postMethod.setEntity(requestBody);

        //When
        CloseableHttpResponse response = httpClient.execute(postMethod);

        //Then
        Assert.assertThat(response.getStatusLine().getStatusCode(),equalTo(HttpStatus.SC_OK));
    }

    @Test
    public void testing_json_payload(){
        //setup

        //execution

        //validation

    }
}
