package com.shortly.apiservice.controller.user;

import com.shortly.apiservice.service.UrlService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    @GetMapping("/{short_key}")
    public void redirect(
            @PathVariable(name = "short_key") String short_key,
            HttpServletResponse response
    ) throws IOException {
        urlService.redirect(short_key, response);
    }
}
