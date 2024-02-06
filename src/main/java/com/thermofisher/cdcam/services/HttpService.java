package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.model.HttpServiceResponse;
import com.thermofisher.cdcam.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class HttpService {


    public HttpServiceResponse post(String url) {
        JSONObject EMPTY_JSON = new JSONObject();
        return post(url, EMPTY_JSON);
    }

    public HttpServiceResponse post(String url, JSONObject body) {
        String requestBody = body.toString();
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);

        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
        httpPost.setEntity(requestEntity);

        HttpServiceResponse response = null;

        try {
            response = execute(httpPost);
        } catch (IOException e) {
            String exception = Utils.stackTraceToString(e);
            log.error(String.format("An error occurred while executing a POST Request. Url: %s. Error: %s", url, exception));
        } catch (JSONException e) {
            String exception = Utils.stackTraceToString(e);
            log.error(String.format("An error occurred while parsing response from POST Request. Url: %s. Error: %s", url, exception));
        }

        return response;
    }

    public HttpServiceResponse put(String url, JSONObject body) {
        String requestBody = body.toString();
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);

        HttpPut httpPut = new HttpPut(url);
        httpPut.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
        httpPut.setEntity(requestEntity);

        HttpServiceResponse response = null;

        try {
            response = execute(httpPut);
        } catch (IOException e) {
            String exception = Utils.stackTraceToString(e);
            log.error(String.format("An error occurred while executing a PUT Request. Url: %s. Error: %s", url, exception));
        } catch (JSONException e) {
            String exception = Utils.stackTraceToString(e);
            log.error(String.format("An error occurred while parsing response from PUT Request. Url: %s. Error: %s", url, exception));
        }

        return response;
    }

    public HttpServiceResponse get(String url) {
        HttpGet httpGet = new HttpGet(url);
        HttpServiceResponse response = null;

        try {
            response = execute(httpGet);
        } catch (IOException e) {
            String exception = Utils.stackTraceToString(e);
            log.error(String.format("An error occurred while executing a GET Request. Url: %s. Error: %s", url, exception));
        } catch (JSONException e) {
            String exception = Utils.stackTraceToString(e);
            log.error(String.format("An error occurred while parsing response from GET Request. Url: %s. Error: %s", url, exception));
        }

        return response;
    }

    private HttpServiceResponse execute(HttpUriRequest request) throws IOException, JSONException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        CloseableHttpResponse closeableResponse = httpClient.execute(request);
        String responseBody = EntityUtils.toString(closeableResponse.getEntity());
        Integer statusCode = closeableResponse.getStatusLine().getStatusCode();
        httpClient.close();

        return HttpServiceResponse.builder()
                .responseBody(new JSONObject(responseBody))
                .status(statusCode)
                .closeableHttpResponse(closeableResponse)
                .build();
    }
}
