package com.makstuk.escapethemaze.backend.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    private static final String SECRET = "dGhpcy1pcy1hLXRlc3Qtc2VjcmV0LXdpdGgtYXQtbGVhc3QtMzItYnl0ZXM=";

    private final JwtTokenService jwtTokenService = new JwtTokenService(SECRET, Duration.ofHours(8));

    @Test
    void parsesATokenCreatedWithTheSameSigningKey() {
        UUID userId = UUID.randomUUID();
        GeneratedToken token = jwtTokenService.generate(userId, "maksym");

        assertThat(jwtTokenService.parse(token.value()))
                .contains(new AuthenticatedUser(userId, "maksym"));
    }

    @Test
    void rejectsAMalformedToken() {
        assertThat(jwtTokenService.parse("not-a-jwt")).isEmpty();
    }
}
