package com.thermofisher.cdcam.services;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    static final Logger logger = LogManager.getLogger("CdcamApp");
    private CloseableHttpClient httpClient = HttpClients.createDefault();

    public CloseableHttpResponse postRequest(String requestBody, String regNotificationUrl) throws IOException {
        HttpPost httpPost = new HttpPost(regNotificationUrl);
        StringEntity body = new StringEntity(requestBody);
        logger.fatal("1. Payload body: " + requestBody);
        httpPost.setEntity(body);
        httpPost.setHeader("Content-Type", "application/json");
        return httpClient.execute(httpPost);
    }
}