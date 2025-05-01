package com.urlshortener.demo.Email;

import com.urlshortener.demo.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailVerificationTokenService {

    @Autowired
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    public EmailVerificationTokenService(EmailVerificationTokenRepository emailVerificationTokenRepository) {
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
    }

    public EmailVerificationToken getVerificationToken(String token) {
        return emailVerificationTokenRepository.findByToken(token);
    }

    public String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    public String createVerificationTokenForUser(User user) {
        String token = generateVerificationToken();

        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(15)); //15 minute expiration timer on token

        emailVerificationTokenRepository.save(verificationToken);

        return token;
    }

}
