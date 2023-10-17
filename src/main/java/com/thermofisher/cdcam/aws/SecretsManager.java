package com.thermofisher.cdcam.aws;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@Slf4j
public class SecretsManager {

    @Value("${aws.sns.client.region}")
    private String region;

    public String getSecret(String secretName) {
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new InstanceProfileCredentialsProvider(false))
                .build();

        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);

        GetSecretValueResult getSecretValueResult;

        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (ResourceNotFoundException e) {
            log.error("The requested secret " + secretName + " was not found");
            throw e;
        } catch (InvalidRequestException e) {
            log.error("The request was invalid due to: " + e.getMessage());
            throw e;
        } catch (InvalidParameterException e) {
            log.error("The request had invalid params: " + e.getMessage());
            throw e;
        }

        log.info(String.format("Secret retrieved successfully: %s", secretName));

        if (getSecretValueResult.getSecretString() != null) {
            return getSecretValueResult.getSecretString();
        } else {
            return new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
        }
    }

    public String getProperty(JSONObject secretProperties, String property) throws JSONException {
        return secretProperties.get(property).toString();
    }
}
