package com.urlshortener.demo.Analytics;

import com.urlshortener.demo.ShortenedUrl.ShortenedUrl;
import com.urlshortener.demo.ShortenedUrl.ShortenedUrlRepository;
import com.urlshortener.demo.ShortenedUrl.ShortenedUrlService;
import com.urlshortener.demo.UrlClick.UrlClick;
import com.urlshortener.demo.UrlClick.UrlClickRepository;
import com.urlshortener.demo.UrlClick.UrlClickService;
import com.urlshortener.demo.User.User;
import com.urlshortener.demo.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")   //All analytics endpoints require ADMIN role.
public class AnalyticsController {

    @Autowired
    private final ShortenedUrlService shortenedUrlService;

    @Autowired
    private final UrlClickRepository urlClickRepository;

    @Autowired
    private final ShortenedUrlRepository shortenedUrlRepository;

    @Autowired
    private final UserRepository userRepository;

    public AnalyticsController(UrlClickService urlClickService, ShortenedUrlService shortenedUrlService, UrlClickRepository urlClickRepository, ShortenedUrlRepository shortenedUrlRepository, UserRepository userRepository) {
        this.shortenedUrlService = shortenedUrlService;
        this.urlClickRepository = urlClickRepository;
        this.shortenedUrlRepository = shortenedUrlRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/urls/clicked-past-week")
    public ResponseEntity<?> getAllShortenedUrlsClickedPastSevenDays() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(6).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Object[]> results = urlClickRepository.countUrlsClickedByDay(sevenDaysAgo);

        // Format result as a list of date/count pairs
        List<Map<String, Object>> response = results.stream().map(obj -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", obj[0].toString());
            map.put("count", ((Number) obj[1]).intValue());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/urls/created-past-week")
    public ResponseEntity<?> getAllShortenedUrlsCreatedPastSevenDays() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(6).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Object[]> results = shortenedUrlRepository.countUrlsCreatedByDay(sevenDaysAgo);

        // Format result as a list of date/count pairs
        List<Map<String, Object>> response = results.stream().map(obj -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", obj[0].toString());
            map.put("count", ((Number) obj[1]).intValue());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-urls")
    public ResponseEntity<?> getTopUrls() { //Gets top 10 urls by click count.

        List<ShortenedUrl> topUrls = shortenedUrlRepository.findTop10ByOrderByClickCountDesc();

        if (topUrls.isEmpty()){
            return ResponseEntity.ok("No URL clicks found.");
        }

        return ResponseEntity.ok(topUrls);

    }

    @GetMapping("/id/{id}")
    public List<UrlClick> getAnalyticsById(@PathVariable Long id){ //Gets url analytics from ShortenedUrl id.

        if (!shortenedUrlService.isValidId(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        //TODO: Expand upon this, can add much more specific or tailored information, such as finding the clicks per day for a certain url, etc.

        return urlClickRepository.findAllByShortUrlId(id); //Returns list of all analytics for given url id.

    }

    @GetMapping("/code/{code}")
    public List<UrlClick> getAnalyticsByShortCode(@PathVariable String code){

        ShortenedUrl shortenedUrl = shortenedUrlService.getShortenedUrlByShortCode(code);

        if (shortenedUrl == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return getAnalyticsById(shortenedUrl.getId());

    }

}
