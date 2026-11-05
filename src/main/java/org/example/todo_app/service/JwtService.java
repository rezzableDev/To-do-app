package org.example.todo_app.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.todo_app.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long ttlMinutes;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.ttl-minutes}") long ttlMinutes) {
        if (secret == null || secret.length() < 32 || secret.startsWith("change-me")) {
            byte[] ephemeralSecret = new byte[48];
            new SecureRandom().nextBytes(ephemeralSecret);
            this.signingKey = Keys.hmacShaKeyFor(ephemeralSecret);
            this.ttlMinutes = ttlMinutes;
            return;
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlMinutes = ttlMinutes;
    }

    public String create(User user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(String.valueOf(user.getId())).claim("email", user.getEmail())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plus(ttlMinutes, ChronoUnit.MINUTES)))
                .signWith(signingKey).compact();
    }

    public Long userId(String token) {
        Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
        return Long.valueOf(claims.getSubject());
    }
}
