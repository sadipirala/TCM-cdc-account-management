package com.thermofisher.cdcam.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thermofisher.cdcam.utils.Utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class URLService {

    @Value("${identity.oidc.authorize.endpoint}")
    String identityAuthorizeEndpoint;

    public String queryParamMapper(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.convertValue(object, Map.class);

        StringBuilder stringQueries = new StringBuilder();
        for (String key : map.keySet()){
            if (Utils.isNullOrEmpty(String.valueOf(map.get(key)))){
                continue;
            }
            stringQueries.append(key);
            stringQueries.append("=");
            stringQueries.append(map.get(key));
            stringQueries.append("&");
        }

        if (stringQueries.length() != 0) {
            stringQueries.deleteCharAt(stringQueries.length() - 1);
        }

        return identityAuthorizeEndpoint.concat("?").concat(stringQueries.toString());
    }
}
