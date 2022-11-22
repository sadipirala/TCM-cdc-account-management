package com.thermofisher.cdcam.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    @Value("${default.login.path}")
    String loginEndpoint;

    public String generateDefaultLoginUrl(String redirectUrl) {
        return String.format("%s?returnUrl=%s", loginEndpoint, redirectUrl);
    }
}
