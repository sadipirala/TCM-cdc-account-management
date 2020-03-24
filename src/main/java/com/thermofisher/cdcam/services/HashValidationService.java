package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class HashValidationService {

    private Logger logger = LogManager.getLogger(this.getClass());
    private String algorithm = "HmacSHA1";

    public String getHashedString(String secretKey, String msg) {
        try {
            byte[] decodedKey = java.util.Base64.getDecoder().decode(secretKey);
            SecretKeySpec key = new SecretKeySpec(decodedKey, algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(key);

            byte[] bytes = mac.doFinal(msg.getBytes());

            return new String(java.util.Base64.getEncoder().encode(bytes));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error(String.format("An error occurred while hashing a string. Error: %s", Utils.stackTraceToString(e)));
        }
        return null;
    }

    public boolean isValidHash(String expected, String sent) {
        return expected.equals(sent);
    }

}
