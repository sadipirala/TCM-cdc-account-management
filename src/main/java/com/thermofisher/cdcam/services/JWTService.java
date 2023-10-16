package com.thermofisher.cdcam.services;

import jakarta.annotation.PostConstruct;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

@Service
public class JWTService {
    private final String JWT_SECRET_NAME = "jwtSecret";
    private Algorithm algorithm;
    private JWTVerifier verifier;

    @Value("${env.name}")
    private String env;

    @Autowired
    SecretsService secretsService;

    @PostConstruct
    public void setup() throws JSONException {
        if (env.equals("local") || env.equals("test")) return;
        String jwtSecret = secretsService.get(JWT_SECRET_NAME);
        algorithm = Algorithm.HMAC256(jwtSecret);
        verifier = JWT.require(algorithm).withIssuer("auth0").build();
    }

    public String create() {
        return JWT.create().withIssuer("auth0").sign(algorithm);
    }

    public void verify(String token) {
        verifier.verify(token);
    }
}
