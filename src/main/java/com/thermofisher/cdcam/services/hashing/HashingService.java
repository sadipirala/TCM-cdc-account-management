package com.thermofisher.cdcam.services.hashing;

import com.google.common.base.Preconditions;

import javax.validation.constraints.NotNull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashingService implements IHashingService {

    private String PASSWORD_ALGORITHM = "MD5";

    @Override
    public String hash(@NotNull String value) throws NoSuchAlgorithmException {
        Preconditions.checkArgument(value != null, "Value cannot be null");

        MessageDigest messageDigest = messageDigest = MessageDigest.getInstance(PASSWORD_ALGORITHM);
        messageDigest.update(value.getBytes());


        byte[] bytes = messageDigest != null ? messageDigest.digest() : new byte[0];

        StringBuilder stringBuilder = new StringBuilder();
        for (byte byteData : bytes) {
            stringBuilder.append(Integer.toString((byteData & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuilder.toString();
    }

    @Override
    public String concat(String value) {
        return String.format("%1$s:%2$s", PASSWORD_ALGORITHM, value);
    }
}
