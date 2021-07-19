package com.thermofisher.cdcam.services;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.thermofisher.cdcam.model.dto.CIPAuthDataDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    @Value("${identity.cookie.cip-authdata.path}")
    String cipAuthdataPath;

    @Autowired
    EncodeService encodeService;

    public String createCIPAuthDataCookie(CIPAuthDataDTO cipAuthData) {
        String cipAuthDataBase64 = new String(encodeService.encodeBase64(new Gson().toJson(cipAuthData)));
        return String.format("cip_authdata=%s; Path=%s", cipAuthDataBase64, cipAuthdataPath);
    }

    public CIPAuthDataDTO decodeCIPAuthDataCookie(String cookieString) throws JsonParseException{
        byte[] cookieStringBytes = cookieString.getBytes();
        String decodedCookie = encodeService.decodeBase64(cookieStringBytes);
        Gson g = new Gson();
        return g.fromJson(decodedCookie, CIPAuthDataDTO.class);
    }
}
