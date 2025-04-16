package com.urlshortener.demo.ShortenedUrl;

import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
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

    public void deleteShortenedUrl(Long id){
        shortenedUrlRepository.deleteById(id);
    }

    public ShortenedUrl getOriginalUrlByShortCode(String code) {
        return shortenedUrlRepository.findByShortCode(code);
    }

    @Transactional //all or nothing, DB transaction method
    public ShortenedUrl createShortenedUrl(String originalUrl, String customLink) {

        String code = createRandomCode();
        while (shortenedUrlRepository.findByShortCode(code) != null){ //Crude way to handle collisions(?), just rerun the algorithm.
            code = createRandomCode();
        }

        if (customLink != null){
            code = customLink;
        }

        LocalDateTime expirationDate = LocalDateTime.now().plusDays(10); //Default expiration date is 10 days from creation date.

        ShortenedUrl shortenedUrl = new ShortenedUrl();
        shortenedUrl.setOriginalUrl(originalUrl);
        shortenedUrl.setExpirationDate(expirationDate);
        shortenedUrl.setCreatedAt(LocalDateTime.now());
        shortenedUrl.setActive(true);
        shortenedUrl.setShortCode(code);
        shortenedUrl.setClickCount(0);

        shortenedUrlRepository.save(shortenedUrl);

        return shortenedUrl;

    }

    public boolean isValidCustomLink(String customLink) {
        ShortenedUrl shortenedUrl = shortenedUrlRepository.findByShortCode(customLink);
        return shortenedUrl == null; //TODO: For now, just checking if custom link does not exist in DB to save it. Length over 8 will throw error, only 8 chars allowed in DB.
    }

    public String createRandomCode(){
        return RandomStringUtils.randomAlphanumeric(8); //creates alphanumeric 8 character String
    }

    public void increaseClickCount(ShortenedUrl shortenedUrl) {
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

}
