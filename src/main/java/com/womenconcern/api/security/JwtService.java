package com.womenconcern.api.security;

import com.womenconcern.api.auth.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /** Access token lifetime in seconds (default 15 min) */
    @Value("${app.jwt.access-token-expiry-seconds:900}")
    private long accessTokenExpirySeconds;

    // ── Token generation ─────────────────────────────────────────

    public String generateAccessToken(User user) {
        return buildToken(user, accessTokenExpirySeconds * 1_000L, Map.of(
                "role",  user.getRole().name(),
                "email", user.getEmail(),
                "name",  user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : ""),
                "User_Id",user.getId()
        ));
    }

    private String buildToken(User user, long expiryMillis, Map<String, Object> extraClaims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getId().toString())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiryMillis))
                .id(UUID.randomUUID().toString())
                .signWith(signingKey())
                .compact();
    }

    // ── Token validation ─────────────────────────────────────────

    public boolean isTokenValid(String token, User user) {
        try {
            String subject = extractSubject(token);
            return subject.equals(user.getId().toString()) && !isTokenExpired(token);
        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ── Claims extraction ─────────────────────────────────────────

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getAccessTokenExpirySeconds() {
        return accessTokenExpirySeconds;
    }
}
