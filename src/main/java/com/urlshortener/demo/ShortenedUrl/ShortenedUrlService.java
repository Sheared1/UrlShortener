package com.urlshortener.demo.ShortenedUrl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.urlshortener.demo.Jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ShortenedUrlService {

    private final ShortenedUrlRepository shortenedUrlRepository;
    private final JwtService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(ShortenedUrlService.class);

    @Value("${app.url}/")
    private String baseUrl;

    public ShortenedUrlService(ShortenedUrlRepository shortenedUrlRepository, JwtService jwtService) {
        this.shortenedUrlRepository = shortenedUrlRepository;
        this.jwtService = jwtService;
    }

    public Page<ShortenedUrl> findWithFilters(
            String shortCode,
            String originalUrl,
            String createdBy,
            String createdAt,
            String clickCount,
            Pageable pageable
    ) {
        Specification<ShortenedUrl> spec = Specification.where(null);

        if (shortCode != null && !shortCode.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("shortCode")), "%" + shortCode.toLowerCase() + "%"));
        }
        if (originalUrl != null && !originalUrl.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("originalUrl")), "%" + originalUrl.toLowerCase() + "%"));
        }
        if (createdBy != null && !createdBy.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("createdBy")), "%" + createdBy.toLowerCase() + "%"));
        }
        if (createdAt != null && !createdAt.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.function("DATE_FORMAT", String.class, root.get("createdAt"), cb.literal("%Y-%m-%d %H:%i:%s")), "%" + createdAt + "%"));
        }
        if (clickCount != null && !clickCount.isEmpty()) {
            try {
                int count = Integer.parseInt(clickCount);
                spec = spec.and((root, query, cb) -> cb.equal(root.get("clickCount"), count));
            } catch (NumberFormatException ignored) {}
        }

        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        return shortenedUrlRepository.findAll(spec, pageable);
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

    @Cacheable(value = "urlCache", unless = "#result == null", key = "#shortCode") //We are using @Cacheable since this is a READ operation. Will check cache before executing.
    public ShortenedUrl getOriginalUrlByShortCode(String shortCode) {
        return shortenedUrlRepository.getShortenedUrlByShortCode(shortCode);
    }

    @Transactional
    //We are using @CachePut since this is a WRITE operation. Will not check cache before executing.
    @CachePut(value = "urlCache", key = "#result.shortCode") //We are caching the shortCode from the RESULT object (ShortenedUrl), which will be the custom link if used.
    public ShortenedUrl createShortenedUrl(String originalUrl, String customLink, LocalDateTime expirationDate, String authHeader) {

        logger.info("Creating shortened URL for original URL: {}", originalUrl);

        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            username = jwtService.extractUsername(token);
        }

        String shortCode = null;

        if (customLink != null) {
            shortCode = customLink;
            logger.info("Using custom shortened URL: {}", shortCode);
        }
        else {
            shortCode = createRandomCode();
            while (shortenedUrlRepository.getShortenedUrlByShortCode(shortCode) != null){ //Handle collisions, rerun the algorithm.
                shortCode = createRandomCode();
            }
        }

        ShortenedUrl shortenedUrl = new ShortenedUrl();
        shortenedUrl.setOriginalUrl(originalUrl);
        shortenedUrl.setExpirationDate(expirationDate); //The default expiration date is null (infinite). User can set it to what they want.
        shortenedUrl.setCreatedAt(LocalDateTime.now());
        shortenedUrl.setActive(true);
        shortenedUrl.setShortCode(shortCode);
        shortenedUrl.setClickCount(0);
        shortenedUrl.setCreatedBy(username);

        //QR Code Generation
        String fullShortenedUrl = baseUrl + "r/" + shortCode;
        try {
            byte[] qrCodeBytes = generateQRCodeImage(fullShortenedUrl, 250, 250); // width, height
            shortenedUrl.setQrCodeImage(qrCodeBytes);
            logger.info("Successfully generated QR code for short code: {}", shortCode);
        } catch (WriterException | IOException e) {
            //For error handling, for now set the QR code to null and log it.
            logger.error("Could not generate QR code for {}: {}", fullShortenedUrl, e.getMessage());
            shortenedUrl.setQrCodeImage(null);
        }

        shortenedUrlRepository.save(shortenedUrl);

        logger.info("Successfully created shortened URL with short code: {} and original URL: {}", shortCode, originalUrl);

        return shortenedUrl;

    }

    private byte[] generateQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }


    public boolean isValidCustomLink(String customLink) {
        if (customLink == null) return true;
        ShortenedUrl shortenedUrl = shortenedUrlRepository.getShortenedUrlByShortCode(customLink);
        return shortenedUrl == null && customLink.matches("^[a-zA-Z0-9_-]{1,8}$");
    }

    public String createRandomCode(){
        logger.info("Creating random shortened URL code");
        return RandomStringUtils.randomAlphanumeric(8); //creates alphanumeric 8 character String
    }

    @Async
    @Transactional
    public void incrementClickCount(ShortenedUrl shortenedUrl) {
        // Fetch the entity again to ensure it is managed
        ShortenedUrl managedUrl = shortenedUrlRepository.getShortenedUrlByShortCode(shortenedUrl.getShortCode());
        if (managedUrl != null) {
            managedUrl.setClickCount(managedUrl.getClickCount() + 1);
            shortenedUrlRepository.save(managedUrl);
        }
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

            // After all potential updates, including expirationDate:
            if (shortenedUrlInDb.getExpirationDate() != null && shortenedUrlInDb.getExpirationDate().isBefore(LocalDateTime.now())) {
                shortenedUrlInDb.setActive(false); // Enforce inactive if expired
            }

            logger.info("Successfully updated shortened URL");

            return shortenedUrlRepository.save(shortenedUrlInDb);
        });

    }

    @Transactional
    public Optional<ShortenedUrl> toggleActiveShortenedUrl(Long id) {
        Optional<ShortenedUrl> shortenedUrl = shortenedUrlRepository.findById(id);

        return shortenedUrlRepository.findById(id).map(shortenedUrlInDb ->{
            LocalDateTime now = LocalDateTime.now();

            // Check if the URL is expired
            if (shortenedUrlInDb.getExpirationDate() != null && shortenedUrlInDb.getExpirationDate().isBefore(now)) {
                shortenedUrlInDb.setActive(false); // If expired, always set to inactive
            } else {
                shortenedUrlInDb.setActive(!shortenedUrlInDb.isActive());
            }

            logger.info("Toggled active status for URL ID {}: new status is {}", id, shortenedUrlInDb.isActive());

            return shortenedUrlRepository.save(shortenedUrlInDb);
        });
    }

    public boolean isValidUrl(String originalUrl) {
        try {
            new URL(originalUrl).toURI();
            return (originalUrl.startsWith("http://") || originalUrl.startsWith("https://"));
        }
        catch (Exception e){ //catches MalformedUrlException
            logger.error("Invalid URL format provided: {}", originalUrl);
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

    //When application sits behind a proxy, getRemoteAddr() would give the proxy IP. Need this method to get the client's actual IP.
    public String getClientIp(HttpServletRequest httpRequest) {
        String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()){
            return xForwardedFor.split(",")[0].trim();
        }
        return httpRequest.getRemoteAddr();
    }

    public List<ShortenedUrl> getShortenedUrlsForUser(String username) {
        return shortenedUrlRepository.findByCreatedByOrderByCreatedAtDesc(username);
    }
}
