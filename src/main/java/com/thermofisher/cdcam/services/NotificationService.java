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

    static final Logger logger = LogManager.getLogger("NotificationService");
    private CloseableHttpClient httpClient = HttpClients.createDefault();

    public int postRequest(String requestBody, String regNotificationUrl) {
        try {

            HttpPost postMethod = new HttpPost(regNotificationUrl);
            StringEntity body = new StringEntity(requestBody);
            postMethod.setEntity(body);
            postMethod.setHeader("Content-type", "application/json");
            CloseableHttpResponse response = httpClient.execute(postMethod);
            int responseCode = response.getStatusLine().getStatusCode();

            logger.info("The call to " + regNotificationUrl + " has finished with response code " + responseCode);
            return responseCode;

        } catch(IOException e){
            logger.error("The call to " + regNotificationUrl + " has failed with errors " + e.getMessage());
            return -1;
        }
    }
}