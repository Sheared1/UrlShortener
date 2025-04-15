package com.urlshortener.demo.ShortenedUrl;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ShortenedUrl {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime expirationDate;

    @Column
    private long clickCount;

    @Column
    private boolean active;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String original_url) {
        this.originalUrl = original_url;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String short_code) {
        this.shortCode = short_code;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public long getClickCount() {
        return clickCount;
    }

    public void setClickCount(long clickCount) {
        this.clickCount = clickCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
//THIS IS A TEST OF GITHUB FUNCTIONALITIES.