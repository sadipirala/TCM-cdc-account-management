/**
 * Reference: https://help.sap.com/viewer/8b8d6fffe113457094a17701f63e3d6a/GIGYA/en-US/417f310970b21014bbc5a10ce4041860.html
 */

package com.thermofisher.cdcam.services;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import com.thermofisher.cdcam.model.cdc.JWTPublicKey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JWTValidator {
    private static final Logger logger = LogManager.getLogger(JWTValidator.class);
    private static final int JWT_HEADER = 0;
    private static final int JWT_PAYLOAD = 1;
    private static final int JWT_SIGNATURE = 2;

    public static boolean isValidSignature(String jwt, JWTPublicKey jwtPublicKey) {
        try {
            String[] jwtParts = jwt.split("[.]");

            final String tokenData = String.join(".", jwtParts[JWT_HEADER], jwtParts[JWT_PAYLOAD]);
            String keySignatureString = jwtParts[JWT_SIGNATURE];
            final String pubKey = jwtPublicKey.getN();
            final String expString = jwtPublicKey.getE();
            String nString = pubKey;

            keySignatureString = keySignatureString.replace('-', '+'); // 62nd char of encoding
            keySignatureString = keySignatureString.replace('_', '/'); // 63rd char of encoding
            byte[] keySignature = Base64.getDecoder().decode(keySignatureString.getBytes());

            nString = nString.replace('-', '+'); // 62nd char of encoding
            nString = nString.replace('_', '/'); // 63rd char of encoding
            byte[] n = Base64.getDecoder().decode(nString.getBytes());
            byte[] e = Base64.getDecoder().decode(expString.getBytes());

            BigInteger nBigInt = new BigInteger(1, n);
            BigInteger eBigInt = new BigInteger(1, e);
            RSAPublicKeySpec rsaPubKey = new RSAPublicKeySpec(nBigInt, eBigInt);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            PublicKey rsa = fact.generatePublic(rsaPubKey);
            Signature rsaSig = Signature.getInstance("SHA256withRSA");
            rsaSig.initVerify(rsa);
            byte[] tokenDataBytes = tokenData.getBytes("UTF-8");
            rsaSig.update(tokenDataBytes);

            return rsaSig.verify(keySignature);
        } catch (InvalidKeySpecException exception) {
            logger.info(String.format("InvalidKeySpecException: %s", exception.getMessage()));
        } catch (SignatureException exception) {
            logger.info(String.format("SignatureException: %s", exception.getMessage()));
        } catch (NoSuchAlgorithmException exception) {
            logger.info(String.format("NoSuchAlgorithmException: %s", exception.getMessage()));
        } catch (Exception exception) {
            logger.info(String.format("Something went wrong while validating the JWT signature: %s", exception.getMessage()));
        }

        return false;
    }
}
