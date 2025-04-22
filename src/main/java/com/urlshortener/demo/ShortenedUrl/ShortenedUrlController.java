package com.urlshortener.demo.ShortenedUrl;

import com.urlshortener.demo.Redis.RedisRateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/urls")
public class ShortenedUrlController {

    @Autowired
    private final ShortenedUrlService shortenedUrlService;
    @Autowired
    private final RedisRateLimitService redisRateLimitService;

    public ShortenedUrlController(ShortenedUrlService shortenedUrlService, RedisRateLimitService redisRateLimitService) {
        this.shortenedUrlService = shortenedUrlService;
        this.redisRateLimitService = redisRateLimitService;
    }

    @GetMapping
    public List<ShortenedUrl> getAllShortenedUrls(){
        return shortenedUrlService.getAllShortenedUrls();
    }

    @GetMapping("/{id}")
    public Optional<ShortenedUrl> getShortenedUrl(@PathVariable Long id){
        return shortenedUrlService.getShortenedUrlById(id);
    }

    @PostMapping("/generate")
    public ResponseEntity<?> createShortenedUrl(@NotNull @RequestBody ShortenedUrlRequest request, HttpServletRequest httpRequest){

        //Rate limiter implementation
        String clientIp = shortenedUrlService.getClientIp(httpRequest);
        if (!redisRateLimitService.allowRequest(clientIp, "GENERATE")){ //Rate limiting implementation, pass in endpoint name.
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded. Try again later.");
        }
        if (!shortenedUrlService.isValidUrl(request.getOriginalUrl())){
            return ResponseEntity.badRequest().body("Error: Invalid URL.");
        }
        if (!shortenedUrlService.isValidLength(request.getOriginalUrl())){
            return ResponseEntity.badRequest().body("Error: URL length greater than 2048 characters.");
        }
        if (!shortenedUrlService.isValidCustomLink(request.getCustomLink())) {
            return ResponseEntity.badRequest().body("Error: Custom code invalid or already exists (code must exist, and be 1-8 alphanumeric characters).");
        }

        ShortenedUrl shortenedUrl = shortenedUrlService.createShortenedUrl(request.getOriginalUrl(), request.getCustomLink()); //If custom link is null, one will be generated.
        return ResponseEntity.status(HttpStatus.CREATED).body(shortenedUrl);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShortenedUrl(@PathVariable Long id){

        if (!shortenedUrlService.isValidId(id)){
            return ResponseEntity.badRequest().body("Error: Invalid ID given");
        }

        shortenedUrlService.deleteShortenedUrl(id);

        return ResponseEntity.status(HttpStatus.OK).body("Deleted successfully.");

    }

    @PutMapping
    public ResponseEntity<?> updateShortenedUrl(@NotNull @RequestBody ShortenedUrlRequest request){

        if (!shortenedUrlService.isValidId(request.getId())){
            return ResponseEntity.badRequest().body("Error: Invalid ID given");
        }

        return ResponseEntity.status(HttpStatus.OK).body(shortenedUrlService.updateShortenedUrl(request));
    }

    @PutMapping("/toggle/{id}")
    public ResponseEntity<?> toggleActiveShortenedUrl(@PathVariable Long id){

        if (!shortenedUrlService.isValidId(id)){
            return ResponseEntity.badRequest().body("Error: Invalid ID given");
        }

        return ResponseEntity.status(HttpStatus.OK).body(shortenedUrlService.toggleActiveShortenedUrl(id));
    }

    @PutMapping("/expiration/{id}")
    public ResponseEntity<?> changeExpirationShortenedUrl(@PathVariable Long id, @RequestBody ShortenedUrlRequest request){
        //Need to pass in LocalDateTime format (yyyy-MM-ddThh:mm:ss.SSS)

        if (!shortenedUrlService.isValidId(id)){
            return ResponseEntity.badRequest().body("Error: Invalid ID given");
        }

        return ResponseEntity.status(HttpStatus.OK).body(shortenedUrlService.changeExpirationShortenedUrl(id, request.getExpirationDate()));

    }

}
