package com.thermofisher.cdcam.services.hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;

public class HashingService {
    private static String PASSWORD_ALGORITHM = "MD5";

    public static String toMD5(String value) throws NoSuchAlgorithmException {
        String hashedValue = hash(value);
        return concat(hashedValue);
    }

    private static String hash(@NotNull String value) throws NoSuchAlgorithmException {
        Preconditions.checkArgument(value != null, "Value cannot be null");
        MessageDigest messageDigest = null;

        messageDigest = MessageDigest.getInstance(PASSWORD_ALGORITHM);
        messageDigest.update(value.getBytes());

        byte[] bytes = messageDigest != null ? messageDigest.digest() : new byte[0];

        StringBuilder stringBuilder = new StringBuilder();
        for (byte byteData : bytes) {
            stringBuilder.append(Integer.toString((byteData & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuilder.toString();
    }

    private static String concat(String value) {
        return String.format("%1$s:%2$s", PASSWORD_ALGORITHM, value).toUpperCase();
    }
}
