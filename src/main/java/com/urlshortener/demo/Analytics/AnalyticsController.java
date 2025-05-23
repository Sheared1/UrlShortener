package com.urlshortener.demo.Analytics;

import com.urlshortener.demo.ShortenedUrl.ShortenedUrl;
import com.urlshortener.demo.ShortenedUrl.ShortenedUrlRepository;
import com.urlshortener.demo.ShortenedUrl.ShortenedUrlService;
import com.urlshortener.demo.UrlClick.UrlClick;
import com.urlshortener.demo.UrlClick.UrlClickRepository;
import com.urlshortener.demo.UrlClick.UrlClickService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

    public AnalyticsController(UrlClickService urlClickService, ShortenedUrlService shortenedUrlService, UrlClickRepository urlClickRepository, ShortenedUrlRepository shortenedUrlRepository) {
        this.shortenedUrlService = shortenedUrlService;
        this.urlClickRepository = urlClickRepository;
        this.shortenedUrlRepository = shortenedUrlRepository;
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
