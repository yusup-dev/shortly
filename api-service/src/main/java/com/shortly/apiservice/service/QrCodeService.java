package com.shortly.apiservice.service;

public interface QrCodeService {

    record QrImage(byte[] data, String contentType, String format) {
    }

    QrImage generate(String shortUrl, String shortKey, int size, String format);
}
