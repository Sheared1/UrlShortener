package com.urlshortener.demo.Email;

import com.urlshortener.demo.Jwt.JwtService;
import com.urlshortener.demo.Redis.RedisRateLimitService;
import com.urlshortener.demo.User.User;
import com.urlshortener.demo.User.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailVerificationController {

    @Autowired
    private final UserService userService;

    @Autowired
    private final EmailVerificationTokenService emailVerificationTokenService;

    @Autowired
    private final JwtService jwtService;

    @Autowired
    private final RedisRateLimitService redisRateLimitService;

    @Autowired
    private final EmailService emailService;

    public EmailVerificationController(UserService userService, EmailVerificationTokenService emailVerificationTokenService, RedisRateLimitService redisRateLimitService, JwtService jwtService, RedisRateLimitService redisRateLimitService1, EmailService emailService) {
        this.userService = userService;
        this.emailVerificationTokenService = emailVerificationTokenService;
        this.jwtService = jwtService;
        this.redisRateLimitService = redisRateLimitService1;
        this.emailService = emailService;
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<?> sendVerificationEmail(@RequestHeader("Authorization") String authHeader, HttpServletRequest httpRequest) {

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User user = userService.findByUsername(username);

        if (!redisRateLimitService.allowRequest(httpRequest.getRemoteAddr(), "RESEND_VERIFICATION_EMAIL")) {
            return ResponseEntity.status(429)
                    .body(Map.of("message", "Rate limit exceeded. Try again later."));
        }
        if (user.isEmailVerified()) {
            System.out.println("EMAIL ALREADY VERIFIED FOUND");
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email is already verified."));
        }

        String verificationToken = emailVerificationTokenService.createVerificationTokenForUser(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        return ResponseEntity.ok("Verification email sent successfully. Please check your email to verify your account.");

    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {

        EmailVerificationToken verificationToken = emailVerificationTokenService.getVerificationToken(token);

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
