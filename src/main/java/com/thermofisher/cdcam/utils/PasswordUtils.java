package com.thermofisher.cdcam.utils;

public class PasswordUtils {
    private static final String PATTERN = "(?=.*[ !\"#$%&'()*+,-./:;<=>?@[\\\\]^_`{|}~])(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?!.*\\\\s).{8,20}$";

    public static boolean isPasswordValid(String password) {
        return password.matches(PATTERN);
    }
}
