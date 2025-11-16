package com.manthan.urlShortener.controller;


import com.manthan.urlShortener.dtos.UrlMappingDTO;
import com.manthan.urlShortener.models.UrlMapping;
import com.manthan.urlShortener.models.User;
import com.manthan.urlShortener.services.UrlMappingService;
import com.manthan.urlShortener.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/urls")
@AllArgsConstructor
public class UrlMappingController {
    private final UrlMappingService urlMappingService;
    private UserService userService;

    @PostMapping("/shorten")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlMappingDTO> createShortUrl(@RequestBody Map<String, String> request, Principal principal){
        String originalUrl = request.get("originalUrl");
        if(originalUrl == null || originalUrl.isBlank()){
            throw new IllegalArgumentException("Original URL is required");
        }

        User user = userService.findByUserName(principal.getName());
        if(user == null){
            throw new IllegalArgumentException("User not found");
        }

        UrlMappingDTO urlMappingDTO = urlMappingService.createShortUrl(originalUrl, user);
        return ResponseEntity.ok(urlMappingDTO);
    }

    @GetMapping("/myurls")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UrlMappingDTO>> getUserUrls(Principal principal)
    {
        User user = userService.findByUserName(principal.getName());
        if(user == null){
            throw new IllegalArgumentException("User not found");
        }

        List<UrlMappingDTO> urlMapping = urlMappingService.getAllUrls(user);
        return ResponseEntity.ok(urlMapping);
    }
}
