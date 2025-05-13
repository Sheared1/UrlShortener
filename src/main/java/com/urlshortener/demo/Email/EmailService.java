package com.urlshortener.demo.Email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.url}")
    private String appUrl;

    @Async
    public void sendVerificationEmail(String to, String token) {

        logger.info("Sending verification email to: " + to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Email Verification");
        message.setText("Please click the link below to verify your email:\n"
                + "http://localhost:8080/api/email/verify-email?token=" + token);

        mailSender.send(message);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {

        logger.info("Sending password reset email to: " + to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Password Reset Request");
        message.setText("Hello,\n\n" +
                "You have requested to reset your password. Please click the link below to set a new password:\n\n" +
                appUrl + "/reset-password.html?token=" + token + "\n\n" +
                "If you did not request this password reset, please ignore this email.\n\n" +
                "This link will expire in 15 minutes.\n\n"
        );

        mailSender.send(message);
    }

}
