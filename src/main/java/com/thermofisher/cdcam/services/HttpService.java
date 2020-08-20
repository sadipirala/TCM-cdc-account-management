package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.model.HttpServiceResponse;
import com.thermofisher.cdcam.utils.Utils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class HttpService {

    private Logger logger = LogManager.getLogger(this.getClass());

    public HttpServiceResponse post(String url, JSONObject body) {
        try {
            logger.info(String.format("Executing POST request to: %s", url));

            String requestBody = body.toString();
            StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);

            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
            httpPost.setEntity(requestEntity);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse closeableResponse = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(closeableResponse.getEntity());

            HttpServiceResponse response = HttpServiceResponse.builder()
                    .responseBody(new JSONObject(responseBody))
                    .closeableHttpResponse(closeableResponse).build();
            httpClient.close();

            return response;
        } catch (Exception e) {
            logger.error(String.format("An error occurred while executing a POST Request. Url: %s. Error: %s", url, Utils.stackTraceToString(e)));
            return null;
        }
    }

    public HttpServiceResponse post(String url) {
        try {
            JSONObject EMPTY_JSON = new JSONObject();
            return post(url, EMPTY_JSON);
        }
        catch (Exception e)
        {
            logger.error(Utils.stackTraceToString(e));
            return null;
        }
    }
}
