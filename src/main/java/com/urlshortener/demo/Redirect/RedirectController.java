package com.urlshortener.demo.Redirect;

import com.urlshortener.demo.ShortenedUrl.ShortenedUrl;
import com.urlshortener.demo.ShortenedUrl.ShortenedUrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;

@Controller
public class RedirectController {

    @Autowired
    private final ShortenedUrlService shortenedUrlService;

    public RedirectController(ShortenedUrlService shortenedUrlService) {
        this.shortenedUrlService = shortenedUrlService;
    }

    @GetMapping("/{code}")
    public String redirect(@PathVariable String code, HttpServletRequest request){

        ShortenedUrl shortenedUrl = shortenedUrlService.getOriginalUrlByShortCode(code);

        //Could add custom responses or redirect to a different webpage, depending on the condition below.
        if (shortenedUrl == null || !shortenedUrl.isActive() || (shortenedUrl.getExpirationDate() != null && shortenedUrl.getExpirationDate().isBefore(LocalDateTime.now()))){
            return "redirect:/404";
        }

        shortenedUrlService.incrementClickCount(shortenedUrl);

        return "redirect:" + shortenedUrl.getOriginalUrl();
    }

}
