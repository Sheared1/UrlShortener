package com.urlshortener.demo.Password;

import com.urlshortener.demo.User.User;
import com.urlshortener.demo.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    @Autowired
    private final PasswordResetService passwordResetService;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final UserService userService;

    public PasswordResetController(PasswordResetService passwordResetService, PasswordEncoder passwordEncoder, UserService userService) {
        this.passwordResetService = passwordResetService;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        //Validate password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Passwords do not match"));
        }

        //Validate password requirements
        if (request.getNewPassword().length() < 8) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Password must be at least 8 characters long"));
        }

        if (!request.getNewPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) {
            return ResponseEntity.badRequest().body("Password must contain at least one uppercase letter, one lowercase letter, and one number");
        }

        //Validate token and get user
        PasswordResetToken resetToken = passwordResetService.findByToken(request.getToken());
        if (resetToken == null || resetToken.isExpired() || resetToken.isUsed()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Password must contain at least one uppercase letter, one lowercase letter, and one number"));
        }

        //Get the user and update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.saveUser(user);

        //Mark token as used
        resetToken.setUsed(true);
        passwordResetService.saveToken(resetToken);

        return ResponseEntity.ok()
                .body(Map.of("message", "Password has been reset successfully"));
    }


    //This mapping VALIDATES the token
    @GetMapping("/reset-password")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        PasswordResetToken resetToken = passwordResetService.findByToken(token);

        if (resetToken == null || resetToken.isExpired() || resetToken.isUsed()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid or expired password reset token"));
        }

        return ResponseEntity.ok()
                .body(Map.of("message", "Token is valid"));

    }


}
