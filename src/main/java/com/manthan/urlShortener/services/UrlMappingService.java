package com.manthan.urlShortener.services;

import com.manthan.urlShortener.Repositories.ClickEventRepository;
import com.manthan.urlShortener.Repositories.UrlMappingRepository;
import com.manthan.urlShortener.dtos.ClickEventDTO;
import com.manthan.urlShortener.dtos.UrlMappingDTO;
import com.manthan.urlShortener.exception.UrlNotFoundException;
import com.manthan.urlShortener.models.ClickEvent;
import com.manthan.urlShortener.models.UrlMapping;
import com.manthan.urlShortener.models.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlMappingService {

    private final UrlMappingRepository urlMappingRepository;
    private final ClickEventRepository clickEventRepository;

    public UrlMappingDTO createShortUrl(String originalUrl, User user) {
        log.info("Creating short URL for user {}", user.getUserName());

        String shortUrl = generateShortUrl();

        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setUser(user);
        urlMapping.setCreatedDate(LocalDateTime.now());

        UrlMapping savedUrl = urlMappingRepository.save(urlMapping);

        log.info("Short URL created: {} -> {}", shortUrl, originalUrl);
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

        log.debug("Generating short URL...");

        do {
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(characters.charAt(random.nextInt(characters.length())));
            }
            shortUrl = sb.toString();
        } while (urlMappingRepository.findByShortUrl(shortUrl) != null);

        log.debug("Generated unique short URL: {}", shortUrl);
        return shortUrl;
    }

    public List<UrlMappingDTO> getAllUrls(User user) {
        log.info("Fetching URLs for user {}", user.getUserName());
        return urlMappingRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<ClickEventDTO> getClickEventsByDate(String shortUrl, LocalDateTime start, LocalDateTime end) {
        log.info("Fetching click analytics for {} from {} to {}", shortUrl, start, end);

        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping == null) {
            log.warn("Short URL {} not found", shortUrl);
            throw new UrlNotFoundException("Short URL not found");
        }

        List<ClickEventDTO> results = clickEventRepository
                .findByUrlMappingAndClickDateBetween(urlMapping, start, end)
                .stream()
                .collect(Collectors.groupingBy(
                        c -> c.getClickDate().toLocalDate(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(e -> new ClickEventDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        log.info("Found {} click analytics records", results.size());
        return results;
    }

    public Map<LocalDate, Long> getTotalClicksByUser(User user, LocalDate start, LocalDate end) {
        log.info("Fetching total clicks for user {} between {} and {}", user.getUserName(), start, end);

        List<UrlMapping> urlMappings = urlMappingRepository.findByUser(user);
        List<ClickEvent> clickEvents = clickEventRepository.findByUrlMappingInAndClickDateBetween(
                urlMappings,
                start.atStartOfDay(),
                end.plusDays(1).atStartOfDay()
        );

        Map<LocalDate, Long> result = clickEvents.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getClickDate().toLocalDate(),
                        Collectors.counting()
                ));

        log.info("Total grouped click count: {}", result.size());
        return result;
    }

    public UrlMapping getOriginalUrl(String shortUrl) {

        log.info("Fetching URL mapping for: {}", shortUrl);

        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);

        if (urlMapping == null) {
            log.warn("Short URL not found: {}", shortUrl);
            throw new UrlNotFoundException("Short URL not found: " + shortUrl);
        }

        // Update click count
        urlMapping.setClickCount(urlMapping.getClickCount() + 1);
        urlMappingRepository.save(urlMapping);
        log.info("Updated click count for {} to {}", shortUrl, urlMapping.getClickCount());

        // Save click event
        ClickEvent clickEvent = new ClickEvent();
        clickEvent.setClickDate(LocalDateTime.now());
        clickEvent.setUrlMapping(urlMapping);
        clickEventRepository.save(clickEvent);

        log.info("Click event saved for {}", shortUrl);

        return urlMapping;
    }
}
