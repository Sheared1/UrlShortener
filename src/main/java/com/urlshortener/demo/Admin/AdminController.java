package com.urlshortener.demo.Admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin.html")
    public String adminPage() {
        return "admin"; //Name of html file without extension
    }

}
