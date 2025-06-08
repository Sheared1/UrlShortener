package com.urlshortener.demo.ShortenedUrl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

}
