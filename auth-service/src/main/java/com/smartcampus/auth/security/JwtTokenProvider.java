package com.smartcampus.auth.security;

import com.smartcampus.auth.exception.TokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            return generateAccessToken(
                    customUserDetails.getId(),
                    customUserDetails.getUsername(),
                    customUserDetails.getRole().name()
            );
        }
        // Fallback - sadece email ile (geriye dönük uyumluluk)
        return generateAccessToken(null, userDetails.getUsername(), null);
    }

    /**
     * Tam bilgi ile JWT Access Token oluşturur.
     * Token payload'ı: { sub: "userId", email: "...", role: "STUDENT|FACULTY|ADMIN", iat: ..., exp: ... }
     */
    public String generateAccessToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        var builder = Jwts.builder()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey());

        // userId varsa subject olarak kullan, yoksa email
        if (userId != null) {
            builder.subject(String.valueOf(userId));
            builder.claim("email", email);
        } else {
            builder.subject(email);
        }

        // Role varsa ekle
        if (role != null) {
            builder.claim("role", role);
        }

        return builder.compact();
    }

    /**
     * @deprecated Yeni generateAccessToken(Long userId, String email, String role) metodunu kullanın
     */
    @Deprecated
    public String generateAccessToken(String email) {
        return generateAccessToken(null, email, null);
    }

    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.error("JWT token expired: {}", ex.getMessage());
            throw TokenException.expired();
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            throw TokenException.invalid();
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
            throw TokenException.invalid();
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
            throw TokenException.invalid();
        }
    }

    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}

