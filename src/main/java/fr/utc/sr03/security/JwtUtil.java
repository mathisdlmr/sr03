package fr.utc.sr03.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Cette classe utilitaire pour les JWT a été inspirée de ce tutoriel en ligne sur la gestion de JWT avec Spring
// https://www.cosmiclearn.com/spring_framework/rest_jwt_authentication.php

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String email) {
        return Jwts.builder()
            .claim("type", "access")
            .setSubject(email)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey())
            .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
            .claim("type", "refresh")
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
            .signWith(getSigningKey())
            .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(parseClaims(token).get("type"));
        } catch (JwtException e) {
            return false;
        }
    }
}
