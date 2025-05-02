package com.urlshortener.demo.User;

import com.urlshortener.demo.Email.EmailService;
import com.urlshortener.demo.Email.EmailVerificationTokenService;
import com.urlshortener.demo.Password.PasswordResetService;
import com.urlshortener.demo.Redis.RedisRateLimitService;
import com.urlshortener.demo.ShortenedUrl.ShortenedUrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private final UserService userService;
    @Autowired
    private final RedisRateLimitService redisRateLimitService;
    @Autowired
    private final ShortenedUrlService shortenedUrlService;
    @Autowired
    private final EmailVerificationTokenService emailVerificationTokenService;
    @Autowired
    private final EmailService emailService;
    @Autowired
    private final PasswordResetService passwordResetService;

    public UserController(UserService userService, RedisRateLimitService redisRateLimitService, ShortenedUrlService shortenedUrlService, EmailVerificationTokenService emailVerificationTokenService, EmailService emailService, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.redisRateLimitService = redisRateLimitService;
        this.shortenedUrlService = shortenedUrlService;
        this.emailVerificationTokenService = emailVerificationTokenService;
        this.emailService = emailService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> sendPasswordResetLink(@RequestBody Map<String, String> request){

        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        User user = userService.findByEmail(email);

        //Always return success even if email does not exist, it is a security best practice.
        if (user != null){
            passwordResetService.createPasswordResetToken(user);
            emailService.sendPasswordResetEmail(email, passwordResetService.findByUser(user).getToken());
        }

        return ResponseEntity.ok("If an account exists with the given email, a password reset link has been sent to the email address.");

    }



    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest user, HttpServletRequest httpRequest){

        System.out.println("Received registration request for username: " + user.getUsername());

        String clientIp = shortenedUrlService.getClientIp(httpRequest);
        if (!redisRateLimitService.allowRequest(clientIp, "REGISTER")){ //Rate limiting implementation, pass in endpoint name.
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded. Try again later.");
        }
        if (userService.findByUsername(user.getUsername()) != null){
            return ResponseEntity.badRequest().body("Error: Username already exists.");
        }
        if (user.getPassword().length() < 8){
            return ResponseEntity.badRequest().body("Error: Password must be at least 8 characters long.");
        }
        if (!user.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")){
            return ResponseEntity.badRequest().body("Error: Password must contain at least one uppercase letter, one lowercase letter, and one number.");
        }
        if (!user.getPassword().matches("^(?=.*[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).+$")){
            return ResponseEntity.badRequest().body("Error: Password must contain at least one special character.");
        }

        if (userService.findByEmail(user.getEmail()) != null){
            return ResponseEntity.badRequest().body("Error: Email already exists.");
        }

        //TODO: add email input validation

        System.out.println("Creating new user: " + user.getUsername());
        User registeredUser = userService.registerUser(user.getUsername(), user.getPassword(), user.getEmail(), "USER");
        System.out.println("User created successfully");


        String verificationToken = emailVerificationTokenService.createVerificationTokenForUser(registeredUser);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        //Not returning User object because we do not want to expose sensitive information.
        UserResponse userResponse = new UserResponse(registeredUser.getUsername(), registeredUser.getRoles());

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully. Please check your email to verify your account.");

    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){

        if (userService.findById(id) == null){
            return ResponseEntity.badRequest().body("Error: User does not exist.");
        }

        userService.deleteUser(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User deleted.");

    }

}
