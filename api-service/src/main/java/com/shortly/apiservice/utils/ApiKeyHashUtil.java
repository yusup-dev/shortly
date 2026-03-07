package com.shortly.apiservice.utils;

import java.security.MessageDigest;
import java.util.HexFormat;

public class ApiKeyHashUtil {

    public static String hash(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
