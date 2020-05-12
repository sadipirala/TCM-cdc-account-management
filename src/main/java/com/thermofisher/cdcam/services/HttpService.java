package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.utils.Utils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HttpService {

    private Logger logger = LogManager.getLogger(this.getClass());

    public CloseableHttpResponse post(String url, JSONObject body) {
        try {
            logger.info(String.format("Executing POST request to: %s", url));
            String requestBody = body.toString();
            StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);

            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(requestEntity);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(httpPost);

            httpClient.close();
            return response;
        } catch (IOException e) {
            logger.error(String.format("An error occurred while executing a POST Request. Url: %s. Error: %s", url, Utils.stackTraceToString(e)));
            return null;
        }
    }
}
