package com.urlshortener.demo.ShortenedUrl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ShortenedUrlServiceTests {

    @Autowired
    private ShortenedUrlService shortenedUrlService;

    @Test
    void shorten_shouldReturnShortenedUrl() {

        String originalUrl = "https://testing.com";
        ShortenedUrl shortenedUrl = shortenedUrlService.shortenUrl(originalUrl, null, null, null);

        assertNotNull(shortenedUrl);
        assertNotNull(shortenedUrl.getShortCode());
        assertEquals(originalUrl, shortenedUrl.getOriginalUrl());

    }

    @Test
    void getShortenedUrlByShortCode_shouldReturnShortenedUrl() {

        String originalUrl = "https://testing.com";
        ShortenedUrl shortenedUrl = shortenedUrlService.shortenUrl(originalUrl, null, null, null);
        String shortCode = shortenedUrl.getShortCode();

        ShortenedUrl retrievedUrl = shortenedUrlService.getShortenedUrlByShortCode(shortCode);

        assertNotNull(retrievedUrl);
        assertEquals(originalUrl, retrievedUrl.getOriginalUrl());
        assertEquals(shortCode, retrievedUrl.getShortCode());

    }

    @Test
    void getShortenedUrlByShortCode_shouldReturnNullForInvalidShortCode() {

        String invalidShortCode = "invalidShortCode";

        ShortenedUrl retrievedUrl = shortenedUrlService.getShortenedUrlByShortCode(invalidShortCode);

        assertNull(retrievedUrl);

    }

    @Test
    void getShortenedUrlById_shouldReturnShortenedUrl() {

        String originalUrl = "https://testing.com";
        ShortenedUrl shortenedUrl = shortenedUrlService.shortenUrl(originalUrl, null, null, null);
        long id = shortenedUrl.getId();

        Optional<ShortenedUrl> optionalUrl = shortenedUrlService.getShortenedUrlById(id);
        ShortenedUrl retrievedUrl = optionalUrl.orElse(null);

        assertNotNull(retrievedUrl);
        assertEquals(originalUrl, retrievedUrl.getOriginalUrl());
        assertEquals(shortenedUrl.getShortCode(), retrievedUrl.getShortCode());

    }

}
