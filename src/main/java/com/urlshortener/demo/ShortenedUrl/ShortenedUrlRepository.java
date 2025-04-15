package com.urlshortener.demo.ShortenedUrl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl, Long> {

    ShortenedUrl findByShortCode(String code);

}
