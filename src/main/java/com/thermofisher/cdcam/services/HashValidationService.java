package com.thermofisher.cdcam.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class HashValidationService {

    final static Logger logger = LogManager.getLogger("CdcamApp");

    String algorithm = "HmacSHA1";

    public String getHashedString(String secretKey, String msg) {
        try {
            byte[] decodedKey = java.util.Base64.getDecoder().decode(secretKey);
            SecretKeySpec key = new SecretKeySpec(decodedKey, algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(key);

            byte[] bytes = mac.doFinal(msg.getBytes());

            return new String(java.util.Base64.getEncoder().encode(bytes));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public boolean isValidHash(String expected, String sent) {
        return expected.equals(sent);
    }

}
