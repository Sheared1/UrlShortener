package com.urlshortener.demo.ShortenedUrl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl, Long> {

    ShortenedUrl getShortenedUrlByShortCode(String code);

    List<ShortenedUrl> findByCreatedByOrderByCreatedAtDesc(String username);

    List<ShortenedUrl> findTop10ByOrderByClickCountDesc();

    @Query("SELECT DATE(s.createdAt) as date, COUNT(s) as count FROM ShortenedUrl s WHERE s.createdAt >= :startDate GROUP BY DATE(s.createdAt) ORDER BY date DESC")
    List<Object[]> countUrlsCreatedByDay(@Param("startDate") LocalDateTime startDate);

    Page<ShortenedUrl> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
