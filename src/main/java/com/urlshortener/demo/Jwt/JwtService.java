package com.urlshortener.demo.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

@Service
public class JwtService {

    @Autowired
    private final JwtUtil jwtUtil;

    public JwtService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public <T> T extractClaim(String token, String claimName, Class<T> claimType){
        Claims claims = extractAllClaims(token);
        //Need special handling for Date type:
        if (claimType == Date.class){
            return claimType.cast(new Date(Long.parseLong(claims.get(claimName).toString())));
        }
        return claimType.cast(claims.get(claimName));
    }

    public String extractUsername(String token) {

        return extractClaim(token, Claims.SUBJECT, String.class);

    }

    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }

    private String generateToken(HashMap<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                //Expiration date will be 1 day
                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24)))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractClaim(token, Claims.SUBJECT, String.class);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims.EXPIRATION, Date.class);
    }

    private Claims extractAllClaims(String token){      //Getting claims/data from JWT
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey(){
        byte[] keyBytes = Base64.getDecoder().decode(jwtUtil.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
