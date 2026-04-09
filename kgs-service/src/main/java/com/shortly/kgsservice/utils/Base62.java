package com.shortly.kgsservice.utils;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Base62 {

    private static final String CHARSET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateRandomKey(int length) {
        StringBuilder result = new StringBuilder();
        BigInteger base = BigInteger.valueOf(CHARSET.length());

        BigInteger randNum = new BigInteger(base.bitLength() * length, RANDOM);

        while (randNum.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divRem = randNum.divideAndRemainder(base);
            int remainder = divRem[1].intValue();
            result.append(CHARSET.charAt(remainder));
            randNum = divRem[0];
        }

        while (result.length() < length) {
            result.append(CHARSET.charAt(0));
        }

        return result.reverse().toString();
    }
}
