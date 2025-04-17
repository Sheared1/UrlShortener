package com.urlshortener.demo.UrlClick;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrlClickRepository extends JpaRepository<UrlClick, Long> {

    List<UrlClick> findAllByShortUrlId(Long id);

}
