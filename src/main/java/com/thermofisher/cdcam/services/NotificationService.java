package com.thermofisher.cdcam.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private CloseableHttpClient httpClient = HttpClients.createDefault();

    public CloseableHttpResponse postRequest(String requestBody, String regNotificationUrl) throws IOException {
        HttpPost httpPost = new HttpPost(regNotificationUrl);
        StringEntity body = new StringEntity(requestBody, StandardCharsets.UTF_8);
        httpPost.setEntity(body);
        httpPost.setHeader("Content-Type", "application/json");
        return httpClient.execute(httpPost);
    }
}