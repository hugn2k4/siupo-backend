package com.siupo.restaurant.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        if (jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters");
        }
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // ===== TOKEN GENERATION =====

    public String generateAccessToken(String email, List<String> roles) {
        return buildToken(email, roles, "ACCESS", accessTokenExpiration);
    }

    public String generateRefreshToken(String email) {
        return buildToken(email, null, "REFRESH", refreshTokenExpiration);
    }

    // ===== TOKEN PARSING =====

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String buildToken(
            String email,
            List<String> roles,
            String type,
            long expiration
    ) {
        return Jwts.builder()
                .setSubject(email)
                .claim("type", type)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isAccessToken(Claims claims) {
        return "ACCESS".equals(claims.get("type"));
    }

    public boolean isRefreshToken(Claims claims) {
        return "REFRESH".equals(claims.get("type"));
    }
}
