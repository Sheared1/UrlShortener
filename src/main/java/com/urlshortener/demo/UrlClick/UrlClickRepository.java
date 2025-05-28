package com.urlshortener.demo.UrlClick;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

@Repository
public interface UrlClickRepository extends JpaRepository<UrlClick, Long> {

    List<UrlClick> findAllByShortUrlId(Long id);

    @Query("SELECT DATE(u.clickedAt) as date, COUNT(u) as count FROM UrlClick u WHERE u.clickedAt >= :startDate GROUP BY DATE(u.clickedAt) ORDER BY date DESC")
    List<Object[]> countUrlsClickedByDay(@Param("startDate") LocalDateTime startDate);

}
