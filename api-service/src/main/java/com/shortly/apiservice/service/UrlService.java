package com.shortly.apiservice.service;

import com.shortly.apiservice.dto.request.UrlRequest;
import com.shortly.apiservice.dto.response.UrlResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface UrlService {
    UrlResponse createUrl(UrlRequest urlRequest, String apiKey, HttpServletRequest request);

    void redirect(String shortKey, HttpServletResponse response) throws IOException;
}
