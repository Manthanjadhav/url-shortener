package com.manthan.urlShortener.services;

import com.manthan.urlShortener.Repositories.UrlMappingRepository;
import com.manthan.urlShortener.dtos.UrlMappingDTO;
import com.manthan.urlShortener.models.UrlMapping;
import com.manthan.urlShortener.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UrlMappingService {
    private final UrlMappingRepository urlMappingRepository;
    public UrlMappingDTO createShortUrl(String originalUrl, User user) {
        String shortUrl = generateShortUrl();
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setUser(user);
        urlMapping.setCreatedDate(LocalDateTime.now());
        UrlMapping savedUrl = urlMappingRepository.save(urlMapping);
        return convertToDto(savedUrl);
    }

    private UrlMappingDTO convertToDto(UrlMapping savedUrl) {
        return UrlMappingDTO.builder()
                .shortUrl(savedUrl.getShortUrl())
                .id(savedUrl.getId())
                .clickCount(savedUrl.getClickCount())
                .createdDate(savedUrl.getCreatedDate())
                .username(savedUrl.getUser().getUserName())
                .originalUrl(savedUrl.getOriginalUrl())
                .build();
    }

    private String generateShortUrl() {
        int length = 8;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();

        String shortUrl;

        do {
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(characters.charAt(random.nextInt(characters.length())));
            }
            shortUrl = sb.toString();
        } while (urlMappingRepository.findByShortUrl(shortUrl) != null);

        return shortUrl;
    }

    public List<UrlMappingDTO> getAllUrls(User user) {
        return urlMappingRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .toList();
    }
}
