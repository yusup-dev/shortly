package com.shortly.apiservice.controller.user;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.shortly.apiservice.service.UrlService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Redirect Controller")
public class RedirectController {

    private final UrlService urlService;

    @GetMapping("/{shortKey}")
    public void redirect(
            @PathVariable String shortKey,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        urlService.redirect(shortKey, request, response);
    }
}
