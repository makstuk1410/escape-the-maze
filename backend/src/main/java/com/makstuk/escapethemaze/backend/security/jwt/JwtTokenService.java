package com.makstuk.escapethemaze.backend.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Creates and verifies signed, short-lived access tokens.
 */
@Service
public class JwtTokenService {

    private final SecretKey signingKey;
    private final Duration expiration;

    public JwtTokenService(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.expiration}") Duration expiration) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.expiration = expiration;
    }

    public GeneratedToken generate(UUID userId, String username) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expiration);

        String value = Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();

        return new GeneratedToken(value, expiresAt);
    }

    public Optional<AuthenticatedUser> parse(String token) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String username = claims.get("username", String.class);

            if (username == null || username.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new AuthenticatedUser(UUID.fromString(claims.getSubject()), username));
        } catch (JwtException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
