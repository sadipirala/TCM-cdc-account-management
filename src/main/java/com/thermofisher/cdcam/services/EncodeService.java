package com.thermofisher.cdcam.services;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class EncodeService {

    public byte[] encodeBase64(String text){
        return Base64.getEncoder().encode(text.getBytes());
    }

    public String decodeBase64(byte[] encodedBytes) {
        return new String(Base64.getDecoder().decode(encodedBytes));
    }

    public String encodeUTF8(String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
    }

    public String decodeUTF8(String text) throws UnsupportedEncodingException {
        return URLDecoder.decode(text, StandardCharsets.UTF_8.toString());
    }
}
