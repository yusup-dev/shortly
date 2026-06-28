package com.shortly.apiservice.service;

import com.shortly.apiservice.dto.request.UpdateUrlRequest;
import com.shortly.apiservice.dto.request.UrlRequest;
import com.shortly.apiservice.dto.request.SearchUrlRequest;
import com.shortly.apiservice.dto.response.UrlResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.UUID;

public interface UrlService {
    UrlResponse createUrl(UrlRequest urlRequest, String apiKey, HttpServletRequest request);
    void redirect(String shortKey, HttpServletResponse response) throws IOException;
    Page<UrlResponse> findAll(SearchUrlRequest request);
    UrlResponse findOne(UUID id);
    UrlResponse update(UUID id, UpdateUrlRequest updateUrlRequest, HttpServletRequest request);
    void delete(UUID id, HttpServletRequest request);

    // admin
    Page<UrlResponse> findAllForAdmin(SearchUrlRequest request);
    UrlResponse findOneForAdmin(UUID id);
    UrlResponse updateForAdmin(UUID id, UpdateUrlRequest updateUrlRequest, HttpServletRequest request);
    void deleteForAdmin(UUID id, HttpServletRequest request);
}
