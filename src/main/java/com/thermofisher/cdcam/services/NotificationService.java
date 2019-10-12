package com.thermofisher.cdcam.services;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NotificationService {
    public int postRequest(String requestBody) throws IOException {
        String regNotificationUrl = "cdcam.reg.notification.url";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost postMethod = new HttpPost(regNotificationUrl);
        StringEntity body = new StringEntity(requestBody);
        postMethod.setEntity(body);
        postMethod.setHeader("Content-type", "application/json");
        try (CloseableHttpResponse response = httpClient.execute(postMethod)) {
            return response.getStatusLine().getStatusCode();
        }
    }
}