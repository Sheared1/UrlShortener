package com.urlshortener.demo.Home;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class HomeController {
    @GetMapping("/")
    public void redirectToIndex(HttpServletResponse response) throws IOException {
        response.sendRedirect("/index.html");
    }
}
