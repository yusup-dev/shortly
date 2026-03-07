package com.shortly.apiservice.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class ApiKeyGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder bash64Encoder = Base64.getUrlEncoder();

    public static String generateApiKey() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return bash64Encoder.encodeToString(randomBytes);
    }

}
