package com.thermofisher.cdcam.services;

import javax.annotation.PostConstruct;

import com.thermofisher.cdcam.aws.SecretsManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SecretsService {
    private JSONObject cdcamSecrets;

    @Value("${cdcam.secrets}")
    private String cdcamSecretsName;

    @Value("${env.name}")
    private String env;

    @Autowired
    SecretsManager secretsManager;
    
    @PostConstruct
    public void setup() throws JSONException {
        if (env.equals("local") || env.equals("test")) return;
        String secret = secretsManager.getSecret(cdcamSecretsName);
        cdcamSecrets = new JSONObject(secret);
    }

    public String get(String secretKey) throws JSONException {
        return secretsManager.getProperty(cdcamSecrets, secretKey);
    }
}
