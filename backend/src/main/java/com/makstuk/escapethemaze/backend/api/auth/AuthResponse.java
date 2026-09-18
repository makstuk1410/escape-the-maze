package com.makstuk.escapethemaze.backend.api.auth;

import java.time.Instant;
import java.util.UUID;

/**
 * Account information and an access token returned after successful authentication.
 */
public record AuthResponse(
        UUID userId,
        String username,
        String email,
        String accessToken,
        String tokenType,
        Instant expiresAt) {
}
