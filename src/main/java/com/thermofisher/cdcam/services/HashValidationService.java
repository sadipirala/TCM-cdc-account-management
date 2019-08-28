package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.aws.SecretsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class HashValidationService {

    final static Logger logger = LogManager.getLogger("CdcamApp");
    @Value("${aws.sns.client.region}")
    private String region;
    @Value("${aws.sns.secret}")
    private String secretName;

    String algorithm="HmacSHA1";

    SecretsManager secretsManager = new SecretsManager();

    public  String getHashedString(String msg){
        try {
            String secretKey =getSecretKeyFromSecretManager();
            byte[] decodedKey = java.util.Base64.getDecoder().decode(secretKey);
            SecretKeySpec key = new SecretKeySpec(decodedKey, algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(key);

            byte[] bytes = mac.doFinal(msg.getBytes());

            return new String( java.util.Base64.getEncoder().encode(bytes));
        }
        catch (NoSuchAlgorithmException | InvalidKeyException e) {
           logger.error(e.getMessage());
        }
        return null;
    }

    public  boolean isValidHash(String expected,String sent)
    {
        return  expected.equals(sent);
    }

    public String getSecretKeyFromSecretManager(){
        try {
            String secretJson = secretsManager.getSecret(region,secretName);
            JSONParser parser = new JSONParser();
            JSONObject secretProperties = (JSONObject) parser.parse(secretJson);
            return secretProperties.get("cdc-secret-key").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
