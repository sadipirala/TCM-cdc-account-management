package com.thermofisher.cdcam.aws;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.thermofisher.cdcam.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
public class SecretsManager {

    @Value("${aws.sns.client.region}")
    private String region;

    private Logger logger = LogManager.getLogger(this.getClass());

    public String getSecret(String secretName){
        logger.info(String.format("Secret requested: %s", secretName));

        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new InstanceProfileCredentialsProvider(false))
                .build();

        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);

        GetSecretValueResult getSecretValueResult;

        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch(ResourceNotFoundException e) {
            logger.error("The requested secret " + secretName + " was not found");
            throw e;
        } catch (InvalidRequestException e) {
            logger.error("The request was invalid due to: " + e.getMessage());
            throw e;
        } catch (InvalidParameterException e) {
            logger.error("The request had invalid params: " + e.getMessage());
            throw e;
        }

        logger.info(String.format("Secret retrieved successfully: %s", secretName));

        if (getSecretValueResult.getSecretString() != null) {
            return getSecretValueResult.getSecretString();
        }
        else {
            return new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
        }
    }

    public String getProperty(JSONObject secretProperties, String property) throws JSONException {
        return secretProperties.get(property).toString();
    }
}
