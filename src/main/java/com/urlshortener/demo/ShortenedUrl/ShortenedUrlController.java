package com.urlshortener.demo.ShortenedUrl;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
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
    public void deleteShortenedUrl(@PathVariable Long id){
        //TODO: change type to responseentity, validate id being given, if id is not valid then throw error.
        shortenedUrlService.deleteShortenedUrl(id);
    }

    @PutMapping("/api/urls")
    public ResponseEntity<?> modifyShortenedUrl(@NotNull @RequestBody ShortenedUrlRequest request){

        Optional<ShortenedUrl> shortenedUrl = shortenedUrlService.getShortenedUrlById(request.getId());

        if (shortenedUrl.isPresent()){
            return ResponseEntity.badRequest().body("Error: Could not find shortened URL for given request.");
        }

        shortenedUrl = shortenedUrlService.updateShortenedUrl(request);

        return ResponseEntity.status(HttpStatus.OK).body(shortenedUrl);
    }

    @PutMapping("/api/urls/toggle/{id}")
    public ResponseEntity<?> toggleActiveShortenedUrl(@PathVariable Long id){

        Optional<ShortenedUrl> shortenedUrl = shortenedUrlService.getShortenedUrlById(id);

        if (shortenedUrl.isEmpty()){
            return ResponseEntity.badRequest().body("Invalid ID given");
        }

        return ResponseEntity.status(HttpStatus.OK).body(shortenedUrlService.toggleActiveShortenedUrl(id));
    }

}
