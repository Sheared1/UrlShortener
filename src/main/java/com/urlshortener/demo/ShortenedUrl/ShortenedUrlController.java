package com.urlshortener.demo.ShortenedUrl;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class ShortenedUrlController {

    @Autowired
    private final ShortenedUrlService shortenedUrlService;

    public ShortenedUrlController(ShortenedUrlService shortenedUrlService) {
        this.shortenedUrlService = shortenedUrlService;
    }

    @GetMapping("/api/urls")
    public List<ShortenedUrl> getAllShortenedUrls(){
        return shortenedUrlService.getAllShortenedUrls();
    }

    @GetMapping("/api/urls/{id}")
    public Optional<ShortenedUrl> getShortenedUrl(@PathVariable Long id){
        return shortenedUrlService.getShortenedUrlById(id);
    }

    @PostMapping("/api/generate")
    public ResponseEntity<?> createShortenedUrl(@NotNull @RequestBody ShortenedUrlRequest request){

        if (!shortenedUrlService.isValidUrl(request.getOriginalUrl())){
            return ResponseEntity.badRequest().body("Error: Invalid URL.");
        }
        if (!shortenedUrlService.isValidCustomLink(request.getCustomLink())){
            return ResponseEntity.badRequest().body("Error: Custom code already exists.");
        }

        ShortenedUrl shortenedUrl = shortenedUrlService.createShortenedUrl(request.getOriginalUrl(), request.getCustomLink()); //If custom link is null, one will be generated.
        return ResponseEntity.status(HttpStatus.CREATED).body(shortenedUrl);

    }

    @DeleteMapping("/api/urls/{id}")
    public ResponseEntity<?> deleteShortenedUrl(@PathVariable Long id){

        if (!shortenedUrlService.isValidId(id)){
            return ResponseEntity.badRequest().body("Error: Invalid ID given");
        }

        shortenedUrlService.deleteShortenedUrl(id);

        return ResponseEntity.status(HttpStatus.OK).body("Deleted successfully.");

    }

    @PutMapping("/api/urls")
    public ResponseEntity<?> updateShortenedUrl(@NotNull @RequestBody ShortenedUrlRequest request){

        if (!shortenedUrlService.isValidId(request.getId())){
            return ResponseEntity.badRequest().body("Error: Invalid ID given");
        }

        return ResponseEntity.status(HttpStatus.OK).body(shortenedUrlService.updateShortenedUrl(request));
    }

    @PutMapping("/api/urls/toggle/{id}")
    public ResponseEntity<?> toggleActiveShortenedUrl(@PathVariable Long id){

        if (!shortenedUrlService.isValidId(id)){
            return ResponseEntity.badRequest().body("Error: Invalid ID given");
        }

        return ResponseEntity.status(HttpStatus.OK).body(shortenedUrlService.toggleActiveShortenedUrl(id));
    }

    @PutMapping("/api/urls/expiration/{id}")
    public ResponseEntity<?> changeExpirationShortenedUrl(@PathVariable Long id, @RequestBody ShortenedUrlRequest request){
        //Need to pass in LocalDateTime format (yyyy-MM-ddThh:mm:ss.SSS)

        if (!shortenedUrlService.isValidId(id)){
            return ResponseEntity.badRequest().body("Error: Invalid ID given");
        }

        return ResponseEntity.status(HttpStatus.OK).body(shortenedUrlService.changeExpirationShortenedUrl(id, request.getExpirationDate()));

    }

}
