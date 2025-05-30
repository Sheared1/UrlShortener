package com.urlshortener.demo.User;

import com.urlshortener.demo.Email.EmailService;
import com.urlshortener.demo.Email.EmailVerificationTokenService;
import com.urlshortener.demo.Jwt.JwtService;
import com.urlshortener.demo.Password.PasswordResetService;
import com.urlshortener.demo.Redis.RedisRateLimitService;
import com.urlshortener.demo.ShortenedUrl.ShortenedUrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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
    @Autowired
    private final JwtService jwtService;

    public UserController(UserService userService, RedisRateLimitService redisRateLimitService, ShortenedUrlService shortenedUrlService, EmailVerificationTokenService emailVerificationTokenService, EmailService emailService, PasswordResetService passwordResetService, JwtService jwtService) {
        this.userService = userService;
        this.redisRateLimitService = redisRateLimitService;
        this.shortenedUrlService = shortenedUrlService;
        this.emailVerificationTokenService = emailVerificationTokenService;
        this.emailService = emailService;
        this.passwordResetService = passwordResetService;
        this.jwtService = jwtService;
    }

    @GetMapping("/get-registered-users")
    public ResponseEntity<?> getRegisteredUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String active,
            @RequestParam(required = false) String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        // If sort param is present, parse and use it
        // Build Specification for filters
        Page<User> usersPage = userService.findWithFilters(id, username, email, createdAt, active, pageable);
        Map<String, Object> response = new HashMap<>();
        response.put("content", usersPage.getContent());
        response.put("totalPages", usersPage.getTotalPages());
        response.put("number", usersPage.getNumber());
        response.put("totalElements", usersPage.getTotalElements());
        response.put("size", usersPage.getSize());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/roles/{userId}")
    public ResponseEntity<?> getUserRoles(@PathVariable Long userId){

        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        Map<String, Object> response = Map.of(
                "username", user.getUsername(),
                "roles", user.getRoles()
        );

        return ResponseEntity.ok(response);

    }

    @PostMapping("/roles/{userId}")
    public ResponseEntity<?> updateUserRoles(@PathVariable Long userId, @RequestBody Map<String, String> roleMap){

        //Add a role to a user:

        String role = roleMap.get("role");

        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        Set<String> roles = user.getRoles();
        roles.add(role);
        user.setRoles(roles);

        userService.saveUser(user);

        Map<String, Object> response = Map.of(
                "username", user.getUsername(),
                "roles", user.getRoles()
        );

        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/roles/{userId}")
    public ResponseEntity<?> deleteUserRole(@PathVariable Long userId, @RequestBody Map<String, String> roleMap){

        //Remove a role from a user:

        String role = roleMap.get("role");

        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        Set<String> roles = user.getRoles();
        if (roles.contains(role)) {
            roles.remove(role);
            user.setRoles(roles);

            userService.saveUser(user);
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Role not found for the user"));
        }

        Map<String, Object> response = Map.of(
                "username", user.getUsername(),
                "roles", user.getRoles()
        );

        return ResponseEntity.ok(response);

    }

    @PostMapping("/toggle-active/{id}")
    public ResponseEntity<?> toggleActive(@RequestHeader("Authorization") String authHeader, @PathVariable Long id, @RequestParam boolean isActive){

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("message", "No bearer token found"));
        }

        return userService.toggleActive(id, isActive);

    }

    @GetMapping("/get-user-roles")
    public ResponseEntity<?> getUserRoles(@RequestHeader("Authorization") String authHeader){

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("message", "No bearer token found"));
        }

        String token = authHeader.substring(7); //Removes "Bearer " prefix
        String username = jwtService.extractUsername(token);
        User user = userService.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        Map<String, Object> response = Map.of(
                "username", user.getUsername(),
                "roles", user.getRoles()
        );

        return ResponseEntity.ok(response);

    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String authHeader){

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("message", "No bearer token found"));
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User user = userService.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        UserProfileDTO userProfileDTO = new UserProfileDTO(
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getBio(),
                user.getDateOfBirth(),
                user.getLocation(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.getRoles(),
                user.isEmailVerified()
        );

        return ResponseEntity.ok(userProfileDTO);

    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody UserProfileDTO userProfileDTO, @RequestHeader("Authorization") String authHeader){

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User user = userService.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        user.setFirstName(userProfileDTO.getFirstName());
        user.setLastName(userProfileDTO.getLastName());
        user.setBio(userProfileDTO.getBio());
        user.setLocation(userProfileDTO.getLocation());
        user.setDateOfBirth(userProfileDTO.getDateOfBirth());

        userService.saveUser(user);

        userProfileDTO.setUsername(user.getUsername());
        userProfileDTO.setFirstName(user.getFirstName());
        userProfileDTO.setLastName(user.getLastName());
        userProfileDTO.setBio(user.getBio());
        userProfileDTO.setLocation(user.getLocation());
        userProfileDTO.setDateOfBirth(user.getDateOfBirth());
        userProfileDTO.setEmail(user.getEmail());
        userProfileDTO.setEmailVerified(user.isEmailVerified());
        userProfileDTO.setCreatedAt(user.getCreatedAt());
        userProfileDTO.setLastLoginAt(user.getLastLoginAt());

        return ResponseEntity.ok(userProfileDTO);

    }

    @GetMapping("/email-verification-status")
    public ResponseEntity<?> getEmailVerificationStatus(@RequestHeader("Authorization") String authHeader){

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("message", "No bearer token found"));
        }

        String token = authHeader.substring(7); //Removes "Bearer " prefix
        String username = jwtService.extractUsername(token);
        User user = userService.findByUsername(username);

        Map<String, Boolean> response = Map.of("emailVerified", user.isEmailVerified());

        return ResponseEntity.ok(response);

    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> sendPasswordResetLink(@RequestBody Map<String, String> request, HttpServletRequest httpRequest){

        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        if (!redisRateLimitService.allowRequest(httpRequest.getRemoteAddr(), "RESET_PASSWORD")) {
            return ResponseEntity.status(429)
                    .body(Map.of("message", "Rate limit exceeded. Try again later."));
        }

        User user = userService.findByEmail(email);

        //Always return success even if email does not exist, it is a security best practice.
        if (user != null){
            passwordResetService.createPasswordResetToken(user);
            emailService.sendPasswordResetEmail(email, passwordResetService.findByUser(user).getToken());
        }

        return ResponseEntity.ok(Map.of("message", "If an account exists with the given email, a password reset link has been sent to the email address."));

    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest user, HttpServletRequest httpRequest){

        logger.info("Received registration request for username: {}", user.getUsername());

        String clientIp = shortenedUrlService.getClientIp(httpRequest);
        if (!redisRateLimitService.allowRequest(clientIp, "REGISTER")){ //Rate limiting implementation, pass in endpoint name.
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("message", "Error: Rate limit exceeded. Try again later."));
        }
        if (userService.findByUsername(user.getUsername()) != null){
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Username already exists."));
        }
        if (user.getPassword().length() < 8){
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Password must be at least 8 characters long."));
        }
        if (!user.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")){
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Password must contain at least one uppercase letter, one lowercase letter, and one number."));
        }
        if (!user.getPassword().matches("^(?=.*[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).+$")){
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Password must contain at least one special character."));
        }
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")){
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Please enter a valid email address."));
        }

        if (userService.findByEmail(user.getEmail()) != null){
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Email already exists."));
        }

        logger.info("Creating new user: {}", user.getUsername());
        User registeredUser = userService.registerUser(user.getUsername(), user.getPassword(), user.getEmail(), "USER");
        logger.info("User created successfully: {}", registeredUser.getUsername());


        String verificationToken = emailVerificationTokenService.createVerificationTokenForUser(registeredUser);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        //Not returning User object because we do not want to expose sensitive information.
        UserResponse userResponse = new UserResponse(registeredUser.getUsername(), registeredUser.getRoles());

        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);

    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){

        if (userService.findById(id) == null){
            return ResponseEntity.badRequest().body(Map.of("message", "Error: User does not exist."));
        }

        userService.deleteUser(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of("message", "User deleted."));

    }

}
