package com.urlshortener.demo.UrlClick;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UrlClickService {

    @Autowired
    private final UrlClickRepository urlClickRepository;

    public UrlClickService(UrlClickRepository urlClickRepository) {
        this.urlClickRepository = urlClickRepository;
    }

    @Async //Async method to not block request, it is not important, just to collect information.
    @Transactional
    public void recordClick(long id, HttpServletRequest request) {

        UrlClick urlClick = new UrlClick();

        urlClick.setClickedAt(LocalDateTime.now());
        urlClick.setShortUrlId(id);
        urlClick.setIpAddress(request.getRemoteAddr()); //Gets IP address from request.
        urlClick.setReferrer(request.getHeader("referer")); //Optional header, indicates URL of the web page from which a request was initiated.
        urlClick.setUserAgent(request.getHeader("User-Agent")); //Optional header, indicates software making the request (browser, OS, version...).

        urlClickRepository.save(urlClick);

    }

}
