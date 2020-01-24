package com.thermofisher.cdcam.services.hashing;

import java.security.NoSuchAlgorithmException;

public interface IHashingService {

    String hash(String value) throws NoSuchAlgorithmException;

    String concat(String value);
}
