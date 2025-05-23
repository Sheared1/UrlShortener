package com.urlshortener.demo.ShortenedUrl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl, Long> {

    ShortenedUrl getShortenedUrlByShortCode(String code);

    List<ShortenedUrl> findByCreatedBy(String username);

    List<ShortenedUrl> findTop10ByOrderByClickCountDesc();
}
