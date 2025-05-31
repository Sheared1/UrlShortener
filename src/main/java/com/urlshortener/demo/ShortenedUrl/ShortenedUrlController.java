package com.urlshortener.demo.ShortenedUrl;

import com.urlshortener.demo.Captcha.CaptchaService;
import com.urlshortener.demo.Jwt.JwtService;
import com.urlshortener.demo.Redis.RedisRateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/urls")
public class ShortenedUrlController {

    @Autowired
    private final ShortenedUrlService shortenedUrlService;
    @Autowired
    private final RedisRateLimitService redisRateLimitService;
    @Autowired
    private final JwtService jwtService;
    @Autowired
    private final CaptchaService captchaService;

    public ShortenedUrlController(ShortenedUrlService shortenedUrlService, RedisRateLimitService redisRateLimitService, JwtService jwtService, CaptchaService captchaService) {
        this.shortenedUrlService = shortenedUrlService;
        this.redisRateLimitService = redisRateLimitService;
        this.jwtService = jwtService;
        this.captchaService = captchaService;
    }

    @Value("${app.url}")
    private String appUrl;

    @GetMapping("/app-url")
    public Map<String, String> getAppUrl() {
        return Map.of("appUrl", appUrl);
    }

    @GetMapping("/qr/{shortCode}")
    public ResponseEntity<?> getQrCode(@PathVariable String shortCode){

        ShortenedUrl shortenedUrl = shortenedUrlService.getShortenedUrlByShortCode(shortCode);

        if (shortenedUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Shortened URL not found for short code: " + shortCode));
        }
        if (shortenedUrl.getQrCodeImage() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "QR code not found for short code: " + shortCode));
        }

        byte[] qrImageBytes = shortenedUrl.getQrCodeImage();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.IMAGE_PNG);
        headers.setContentLength(qrImageBytes.length);

        return new ResponseEntity<>(qrImageBytes, headers, HttpStatus.OK);

    }

    @GetMapping("/all")
    public List<ShortenedUrl> getAllShortenedUrls(){
        return shortenedUrlService.getAllShortenedUrls();
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getShortenedUrlsForUser(@PathVariable String username, @RequestHeader("Authorization") String authHeader){
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String authenticatedUsername = jwtService.extractUsername(token);

            if (!username.equals(authenticatedUsername)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You can only access your own URLs."));
            }

            return ResponseEntity.ok(shortenedUrlService.getShortenedUrlsForUser(username));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No bearer token found"));


    }

    @GetMapping("/{id}")
    public Optional<ShortenedUrl> getShortenedUrl(@PathVariable Long id){
        return shortenedUrlService.getShortenedUrlById(id);
    }

    @PostMapping("/generate")
    public ResponseEntity<?> createShortenedUrl(@NotNull @RequestBody ShortenedUrlRequest request, HttpServletRequest httpRequest, @RequestHeader(value = "Authorization", required = false) String authHeader){

        //Rate limiter implementation
        String clientIp = shortenedUrlService.getClientIp(httpRequest);
        if (!redisRateLimitService.allowRequest(clientIp, "GENERATE")){ //Rate limiting implementation, pass in endpoint name.
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("message", "Rate limit exceeded. Try again later."));
        }
        //Captcha validation
        if (!captchaService.validateCaptcha(request.getCaptchaToken())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid captcha"));
        }
        if (!shortenedUrlService.isValidUrl(request.getOriginalUrl())){
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Invalid URL."));
        }
        if (!shortenedUrlService.isValidLength(request.getOriginalUrl())){
            return ResponseEntity.badRequest().body(Map.of("message", "Error: URL length greater than 2048 characters."));
        }
        if (!shortenedUrlService.isValidCustomLink(request.getCustomLink())) {
            System.out.println("Custom link is invalid");
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Custom code invalid or already exists (code must exist, and be 1-8 alphanumeric characters)."));
        }

        ShortenedUrl shortenedUrl = shortenedUrlService.createShortenedUrl(request.getOriginalUrl(), request.getCustomLink(), request.getExpirationDate(), authHeader); //If custom link is null, one will be generated.
        return ResponseEntity.status(HttpStatus.CREATED).body(shortenedUrl);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShortenedUrl(@PathVariable Long id){

        if (!shortenedUrlService.isValidId(id)){
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Invalid ID given"));
        }

        shortenedUrlService.deleteShortenedUrl(id);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Deleted successfully."));

    }

    @PutMapping
    public ResponseEntity<?> updateShortenedUrl(@NotNull @RequestBody ShortenedUrlRequest request){

        if (!shortenedUrlService.isValidId(request.getId())){
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Invalid ID given"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Shortened URL updated to: " + shortenedUrlService.updateShortenedUrl(request) + "."));
    }

    @PutMapping("/toggle/{id}")
    public ResponseEntity<?> toggleActiveShortenedUrl(@PathVariable Long id){

        if (!shortenedUrlService.isValidId(id)){
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Invalid ID given"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(shortenedUrlService.toggleActiveShortenedUrl(id));
    }

    @PutMapping("/expiration/{id}")
    public ResponseEntity<?> changeExpirationShortenedUrl(@PathVariable Long id, @RequestBody ShortenedUrlRequest request){
        //Need to pass in LocalDateTime format (yyyy-MM-ddThh:mm:ss.SSS)

        if (!shortenedUrlService.isValidId(id)){
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Invalid ID given"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Expiration date updated successfully to " + request.getExpirationDate() + "."));

    }

}
