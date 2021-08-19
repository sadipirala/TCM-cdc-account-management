package com.thermofisher.cdcam.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service
public class IdentityAuthorizationService {
    private Logger logger = LogManager.getLogger(this.getClass());

    @Value("${identity.oidc.authorize}")
    String authorize;

    @Value("${identity.oidc.client_id}")
    String clientId;

    @Value("${identity.oidc.redirect_uri}")
    String identityRedirectUri;

    @Value("${identity.oidc.scope}")
    String scope;

    @Value("${identity.oidc.state}")
    String state;

    @Value("${identity.oidc.u}")
    String u;

    public String generateDefaultRedirectSignInUrl() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(authorize).append(clientId).append(identityRedirectUri).append(scope).append(state).toString();
        return urlBuilder.toString();
    }

    public String generateRedirectAuthUrl(String redirectUrl) {
        String uString = "{\"u\":\"" + u.concat(redirectUrl) + "\"}";
        String uEncoded = null;
        try {
            uEncoded = URLEncoder.encode(uString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
        }
        
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(authorize).append(clientId).append(identityRedirectUri).append(scope).append(state).append(uEncoded).toString();
        return urlBuilder.toString();
    }

    public String buildDefaultStateProperty(String redirectUrl) {
        return String.format("{\"u\":\"%s\"}", u.concat(redirectUrl));
    }
}
