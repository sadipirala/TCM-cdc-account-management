package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.model.Ciphertext;
import com.thermofisher.cdcam.model.HttpServiceResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service
public class DataProtectionService {
    private Logger logger = LogManager.getLogger(this.getClass());

    @Value("${data.protection.decryption.url}")
    private String decryptEndpoint;

    @Autowired
    HttpService httpService;

    public JSONObject decrypt(String encryptedData) throws UnsupportedEncodingException {
        String url = String.format("%s?ciphertext=%s", decryptEndpoint, URLEncoder.encode(encryptedData, "UTF-8"));
        HttpServiceResponse response = httpService.get(url);
        return response.getResponseBody();
    }

    public Ciphertext decrypCiphertext(String ciphertext) throws JSONException, UnsupportedEncodingException {
        JSONObject decryptedCiphertext = decrypt(ciphertext);
        JSONObject body = decryptedCiphertext.getJSONObject("body");
        logger.info(String.format("Decrypted ciphertext: %s", decryptedCiphertext.toString()));
        String source = body.has("source") ? body.getString("source") : null;
        return Ciphertext.builder()
                .firstName(body.getString("firstName"))
                .lastName(body.getString("lastName"))
                .email(body.getString("email"))
                .source(source)
                .build();
    }
}
