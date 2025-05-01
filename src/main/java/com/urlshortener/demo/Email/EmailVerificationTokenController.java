package com.urlshortener.demo.Email;

import com.urlshortener.demo.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/email")
public class EmailVerificationTokenController {

    @Autowired
    private final UserService userService;

    @Autowired
    private final EmailVerificationTokenService emailVerificationTokenService;

    public EmailVerificationTokenController(UserService userService, EmailVerificationTokenService emailVerificationTokenService) {
        this.userService = userService;
        this.emailVerificationTokenService = emailVerificationTokenService;
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenService.getVerificationToken(token); //TODO: implement getting token from db

        if (verificationToken == null) {
            return ResponseEntity.badRequest().body("Invalid verification token");
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Verification token has expired");
        }

        userService.setEmailVerified(verificationToken.getUser(), true);

        return ResponseEntity.ok("Email verified successfully");
    }

}
