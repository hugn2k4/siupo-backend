package com.siupo.restaurant.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static javax.crypto.Cipher.SECRET_KEY;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // ====== ACCESS TOKEN ======
    public String generateAccessToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ====== REFRESH TOKEN ======
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ====== LẤY EMAIL TỪ TOKEN ======
    public String getEmailFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi lấy email từ token: " + e.getMessage());
            return null;
        }
    }

    // ====== KIỂM TRA TOKEN ======
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("❌ Token đã hết hạn: " + e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            System.out.println("❌ Token không được hỗ trợ: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.out.println("❌ Token sai định dạng: " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            System.out.println("❌ Chữ ký token không hợp lệ: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Token rỗng hoặc không hợp lệ: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("❌ Lỗi không xác định khi validate token: " + e.getMessage());
            return false;
        }
    }

    // ====== KIỂM TRA TOKEN VÀ LẤY THÔNG TIN ======
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true; // Nếu có lỗi thì coi như token đã hết hạn
        }
    }

    // ====== LẤY THỜI GIAN HẾT HẠN CỦA TOKEN ======
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration();
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi lấy expiration date từ token: " + e.getMessage());
            return null;
        }
    }

    // ====== GETTER CHO EXPIRATION TIMES ======
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
