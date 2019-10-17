package com.thermofisher.cdcam.services;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NotificationService {
    static final Logger logger = LogManager.getLogger("CdcamApp");

    private CloseableHttpClient httpClient = HttpClients.createDefault();

    public CloseableHttpResponse postRequest(String requestBody, String regNotificationUrl) throws IOException {

            HttpPost postMethod = new HttpPost(regNotificationUrl);
            StringEntity body = new StringEntity(requestBody);
            logger.fatal("Payload body: " + requestBody);
            postMethod.setEntity(body);
            postMethod.setHeader("Content-type", "application/json");
            return httpClient.execute(postMethod);
    }
}