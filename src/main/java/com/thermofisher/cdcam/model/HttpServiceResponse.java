package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.json.JSONObject;

@Builder
@Getter
@Setter
public class HttpServiceResponse {
    private CloseableHttpResponse closeableHttpResponse;
    private JSONObject responseBody;
}
