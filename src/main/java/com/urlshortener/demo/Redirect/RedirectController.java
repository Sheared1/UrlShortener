package com.urlshortener.demo.Redirect;

import com.urlshortener.demo.ShortenedUrl.ShortenedUrl;
import com.urlshortener.demo.ShortenedUrl.ShortenedUrlService;
import com.urlshortener.demo.UrlClick.UrlClickService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

//Needs to be controller to support redirects, Controller returns a view, RestController responds with a response body.
@Controller
public class RedirectController {

    @Autowired
    private final ShortenedUrlService shortenedUrlService;
    @Autowired
    private final UrlClickService urlClickService;

    public RedirectController(ShortenedUrlService shortenedUrlService, UrlClickService urlClickService) {
        this.shortenedUrlService = shortenedUrlService;
        this.urlClickService = urlClickService;
    }

    @GetMapping("/r/{code}")
    public String redirect(@PathVariable String code, HttpServletRequest request){

        ShortenedUrl shortenedUrl = shortenedUrlService.getOriginalUrlByShortCode(code);

        //Could add custom responses or redirect to a different webpage, depending on the condition below.
        if (shortenedUrl == null || !shortenedUrl.isActive() || (shortenedUrl.getExpirationDate() != null && shortenedUrl.getExpirationDate().isBefore(LocalDateTime.now()))){
            return "redirect:/404";
        }

        shortenedUrlService.incrementClickCount(shortenedUrl); //Async

        urlClickService.recordClick(shortenedUrl.getId(), request); //Async


        return "redirect:" + shortenedUrl.getOriginalUrl();
    }

}
