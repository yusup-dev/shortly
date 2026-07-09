package com.shortly.apiservice.service;

public interface QrCodeService {

    byte[] generate(String shortUrl, String shortKey, int size);
}
