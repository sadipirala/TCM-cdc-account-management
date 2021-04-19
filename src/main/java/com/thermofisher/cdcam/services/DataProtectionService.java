package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.model.HttpServiceResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service
public class DataProtectionService {

    @Value("${data.protection.decryption.url}")
    private String decryptionEndPoint;

    @Autowired
    HttpService httpService;

    public JSONObject decrypt(String encryptedData) throws JSONException, UnsupportedEncodingException {
        String url = decryptionEndPoint;
        HttpServiceResponse response = httpService.get(url + "?ciphertext=" + URLEncoder.encode(encryptedData, "UTF-8") );
        return response.getResponseBody();
    }
}
