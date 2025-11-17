package com.manthan.urlShortener.controller;

import com.manthan.urlShortener.dtos.ClickEventDTO;
import com.manthan.urlShortener.dtos.UrlMappingDTO;
import com.manthan.urlShortener.exception.UserNotFoundException;
import com.manthan.urlShortener.services.UrlMappingService;
import com.manthan.urlShortener.services.UserService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/urls")
@AllArgsConstructor
@Slf4j
public class UrlMappingController {

    private final UrlMappingService urlMappingService;
    private final UserService userService;

    @PostMapping("/shorten")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlMappingDTO> createShortUrl(@RequestBody Map<String, String> request,
                                                        Principal principal) {

        log.info("Request to shorten URL by user {}", principal.getName());

        String originalUrl = request.get("originalUrl");
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new IllegalArgumentException("Original URL is required");
        }

        var user = userService.findByUserName(principal.getName());
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        UrlMappingDTO dto = urlMappingService.createShortUrl(originalUrl, user);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/myurls")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UrlMappingDTO>> getUserUrls(Principal principal) {
        log.info("User {} is requesting all URLs", principal.getName());

        var user = userService.findByUserName(principal.getName());
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        return ResponseEntity.ok(urlMappingService.getAllUrls(user));
    }

    @GetMapping("/analytics/{shortUrl}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ClickEventDTO>> getUrlAnalytics(@PathVariable String shortUrl,
                                                               @RequestParam("start") String startDate,
                                                               @RequestParam("end") String endDate) {

        log.info("Analytics request for {} from {} to {}", shortUrl, startDate, endDate);

        LocalDateTime start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return ResponseEntity.ok(urlMappingService.getClickEventsByDate(shortUrl, start, end));
    }

    @GetMapping("/totalClicks")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<LocalDate, Long>> getTotalClicks(Principal principal,
                                                               @RequestParam("start") String startDate,
                                                               @RequestParam("end") String endDate) {

        log.info("Total clicks request by {} from {} to {}", principal.getName(), startDate, endDate);

        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);

        var user = userService.findByUserName(principal.getName());
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        return ResponseEntity.ok(urlMappingService.getTotalClicksByUser(user, start, end));
    }
}
