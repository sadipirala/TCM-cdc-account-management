package com.thermofisher.cdcam.builders;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSRequest;

public class GSRequestFactory {
    private static boolean useHTTPS = true;

    public static GSRequest create(String apiKey, String secretKey, String apiDomain, String apiMethod) {
        GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, useHTTPS);
        request.setAPIDomain(apiDomain);
        return request;
    }

    public static GSRequest createWithParams(String apiKey, String secretKey, String apiDomain, String apiMethod, GSObject params) {
        GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, params, useHTTPS);
        request.setAPIDomain(apiDomain);
        return request;
    }
}
