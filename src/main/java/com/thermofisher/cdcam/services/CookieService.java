package com.thermofisher.cdcam.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.thermofisher.cdcam.enums.CookieType;
import com.thermofisher.cdcam.model.dto.CIPAuthDataDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    @Autowired
    EncodeService encodeService;

    @Value("${identity.authorization.cookie.cip-authdata.domain}")
    String cipAuthDataDomain;

    @Value("${identity.reset-password.oidc.rp.client_id}")
    String identityResetPasswordClientId;

    @Value("${identity.reset-password.oidc.rp.scope}")
    String identityResetPasswordScope;

    @Value("${identity.reset-password.oidc.rp.redirect_uri}")
    String identityResetPasswordRedirectUri;

    @Value("${identity.reset-password.oidc.rp.response_type}")
    String identityResetPasswordResponseType;

    public String createCIPAuthDataCookie(CIPAuthDataDTO cipAuthData, String path) {
        String cipAuthDataBase64 = new String(encodeService.encodeBase64(new GsonBuilder().disableHtmlEscaping().create().toJson(cipAuthData)));
        return String.format("cip_authdata=%s; Path=%s; Domain=%s", cipAuthDataBase64, path, cipAuthDataDomain);
    }

    public CIPAuthDataDTO decodeCIPAuthDataCookie(String cookieString) throws JsonParseException{
        byte[] cookieStringBytes = cookieString.getBytes();
        String decodedCookie = encodeService.decodeBase64(cookieStringBytes);
        Gson g = new Gson();
        return g.fromJson(decodedCookie, CIPAuthDataDTO.class);
    }

    public String buildDefaultCipAuthDataCookie(CookieType type) {
        switch (type){
            case RESET_PASSWORD:
                CIPAuthDataDTO cipAuthDataDTO = CIPAuthDataDTO.builder()
                        .clientId(identityResetPasswordClientId)
                        .redirectUri(identityResetPasswordRedirectUri)
                        .responseType(identityResetPasswordResponseType)
                        .scope(identityResetPasswordScope)
                        .build();
                return new String(encodeService.encodeBase64(new GsonBuilder().disableHtmlEscaping().create().toJson(cipAuthDataDTO)));
            default:
                return "";
        }
    }
}
