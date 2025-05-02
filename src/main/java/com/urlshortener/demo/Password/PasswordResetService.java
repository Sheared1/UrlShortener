package com.urlshortener.demo.Password;

import com.urlshortener.demo.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordResetService {

    @Autowired
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetService(PasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    public PasswordResetToken createPasswordResetToken(User user){

        //Delete any existing token for this user
        PasswordResetToken existingToken = passwordResetTokenRepository.findByUser(user);
        if (existingToken != null) {
            passwordResetTokenRepository.delete(existingToken);
        }

        //Generate new token
        String token = generateSecureToken();

        //Create expiry of 15 minutes
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

        //Create and save new token
        PasswordResetToken passwordResetToken = new PasswordResetToken(user, token, expiryDate);
        return passwordResetTokenRepository.save(passwordResetToken);

    }

    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    public PasswordResetToken findByUser(User user){
        return passwordResetTokenRepository.findByUser(user);
    }

    public PasswordResetToken findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    public void saveToken(PasswordResetToken resetToken) {
        passwordResetTokenRepository.save(resetToken);
    }
}
