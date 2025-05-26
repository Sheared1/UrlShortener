package com.urlshortener.demo.Authentication;

import com.urlshortener.demo.Email.EmailService;
import com.urlshortener.demo.Jwt.JwtService;
import com.urlshortener.demo.User.CustomUserDetailsService;
import com.urlshortener.demo.User.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private final JwtService jwtService;

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    public AuthenticationController(JwtService jwtService, AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService, UserService userService) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthenticationRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            userService.updateLastLoginTime(userDetails.getUsername());
            String token = jwtService.generateToken(userDetails);
            return ResponseEntity.ok(new AuthenticationResponse(token));
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid username or password"));
        } catch (InternalAuthenticationServiceException e) {
            if (e.getCause() instanceof LockedException) {
                return ResponseEntity.badRequest().body(Map.of("message", "User account is locked"));
            }
            return ResponseEntity.badRequest().body(Map.of("message", "Authentication failed"));
        }
    }

}
