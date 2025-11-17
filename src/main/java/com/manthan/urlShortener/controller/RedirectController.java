package com.manthan.urlShortener.controller;

import com.manthan.urlShortener.exception.UrlNotFoundException;
import com.manthan.urlShortener.models.UrlMapping;
import com.manthan.urlShortener.services.UrlMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlMappingService urlMappingService;

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl) {
        log.info("Redirect request received for short URL: {}", shortUrl);

        UrlMapping urlMapping = urlMappingService.getOriginalUrl(shortUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", urlMapping.getOriginalUrl());

        log.info("Redirecting to: {}", urlMapping.getOriginalUrl());

        return ResponseEntity.status(302).headers(headers).build();
    }
}
