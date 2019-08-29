package com.thermofisher.cdcam.aws;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Base64;

public class SecretsManager {

    final static Logger logger = LogManager.getLogger("AWSSecretManager");

    public String getSecret(String secretName,String clientRegion){
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(clientRegion)
                .withCredentials(new InstanceProfileCredentialsProvider(false))
                .build();
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult;
        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        }catch (Exception e){
            throw e;
        }
        if (getSecretValueResult.getSecretString() != null) {
            return getSecretValueResult.getSecretString();
        }
        else {
            return new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
        }
    }
}
