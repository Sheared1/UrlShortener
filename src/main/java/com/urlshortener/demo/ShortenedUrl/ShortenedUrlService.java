package com.urlshortener.demo.ShortenedUrl;

import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//TODO: Add logger
@Service
public class ShortenedUrlService {

    private final ShortenedUrlRepository shortenedUrlRepository;

    public ShortenedUrlService(ShortenedUrlRepository shortenedUrlRepository) {
        this.shortenedUrlRepository = shortenedUrlRepository;
    }

    public List<ShortenedUrl> getAllShortenedUrls(){
        return shortenedUrlRepository.findAll();
    }

    public Optional<ShortenedUrl> getShortenedUrlById(Long id){
        return shortenedUrlRepository.findById(id);
    }

    @Transactional
    public void deleteShortenedUrl(Long id){
        shortenedUrlRepository.deleteById(id);
    }

    @Cacheable(value = "urlCache", key = "#shortCode") //We are using @Cacheable since this is a READ operation. Will check cache before executing.
    public ShortenedUrl getOriginalUrlByShortCode(String shortCode) {
        return shortenedUrlRepository.getShortenedUrlByShortCode(shortCode);
    }

    @Transactional
    //We are using @CachePut since this is a WRITE operation. Will not check cache before executing.
    @CachePut(value = "urlCache", key = "#result.shortCode") //We are caching the shortCode from the RESULT object (ShortenedUrl), which will be the custom link if used.
    public ShortenedUrl createShortenedUrl(String originalUrl, String customLink) {

        String shortCode = createRandomCode();
        while (shortenedUrlRepository.getShortenedUrlByShortCode(shortCode) != null){ //(Crude?) way to handle collisions, just rerun the algorithm.
            shortCode = createRandomCode();
        }

        if (customLink != null){
            shortCode = customLink;
        }

        LocalDateTime expirationDate = LocalDateTime.now().plusDays(10); //Default expiration date is 10 days from creation date.

        ShortenedUrl shortenedUrl = new ShortenedUrl();
        shortenedUrl.setOriginalUrl(originalUrl);
        shortenedUrl.setExpirationDate(expirationDate);
        shortenedUrl.setCreatedAt(LocalDateTime.now());
        shortenedUrl.setActive(true);
        shortenedUrl.setShortCode(shortCode);
        shortenedUrl.setClickCount(0);

        shortenedUrlRepository.save(shortenedUrl);

        return shortenedUrl;

    }

    public boolean isValidCustomLink(String customLink) {
        ShortenedUrl shortenedUrl = shortenedUrlRepository.getShortenedUrlByShortCode(customLink);
        return shortenedUrl == null && customLink != null && customLink.matches("^[a-zA-Z0-9_-]{1,8}$");
    }

    public String createRandomCode(){
        return RandomStringUtils.randomAlphanumeric(8); //creates alphanumeric 8 character String
    }

    @Async
    @Transactional
    public void incrementClickCount(ShortenedUrl shortenedUrl) {
        shortenedUrl.setClickCount(shortenedUrl.getClickCount() + 1);
        shortenedUrlRepository.save(shortenedUrl);
    }

    @Transactional
    public Optional<ShortenedUrl> updateShortenedUrl(ShortenedUrlRequest request) {

        return shortenedUrlRepository.findById(request.getId()).map(shortenedUrlInDb ->{
            shortenedUrlInDb.setId(request.getId());
            shortenedUrlInDb.setActive(request.isActive());
            shortenedUrlInDb.setOriginalUrl(request.getOriginalUrl());
            shortenedUrlInDb.setCreatedAt(request.getCreatedAt());
            shortenedUrlInDb.setExpirationDate(request.getExpirationDate());
            shortenedUrlInDb.setClickCount(request.getClickCount());
            shortenedUrlInDb.setShortCode(request.getShortCode());

            return shortenedUrlRepository.save(shortenedUrlInDb);
        });

    }

    @Transactional
    public Optional<ShortenedUrl> toggleActiveShortenedUrl(Long id) {
        Optional<ShortenedUrl> shortenedUrl = shortenedUrlRepository.findById(id);

        return shortenedUrlRepository.findById(id).map(shortenedUrlInDb ->{
            shortenedUrlInDb.setActive(!shortenedUrlInDb.isActive());
            return shortenedUrlRepository.save(shortenedUrlInDb);
        });
    }

    public boolean isValidUrl(String originalUrl) {
        try {
            new URL(originalUrl).toURI();
            return (originalUrl.startsWith("http://") || originalUrl.startsWith("https://"));
        }
        catch (Exception e){ //catches MalformedUrlException
            return false;
        }
    }

    public boolean isValidLength(String originalUrl){
        return originalUrl.length() <= 2048;
    }

    @Transactional
    public Object changeExpirationShortenedUrl(Long id, LocalDateTime expirationDate) {

        return shortenedUrlRepository.findById(id).map(shortenedUrlInDb -> {
            shortenedUrlInDb.setExpirationDate(expirationDate);
            return shortenedUrlRepository.save(shortenedUrlInDb);
        });

    }

    public boolean isValidId(Long id){
        Optional<ShortenedUrl> shortenedUrl = getShortenedUrlById(id);
        return shortenedUrl.isPresent();
    }

    public ShortenedUrl getShortenedUrlByShortCode(String shortCode) {
        return shortenedUrlRepository.getShortenedUrlByShortCode(shortCode);
    }
}
