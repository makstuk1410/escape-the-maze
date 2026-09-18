package com.makstuk.escapethemaze.backend.api.auth;

import java.time.Instant;
import java.util.UUID;

/**
 * Account data that is safe to return after successful registration.
 */
public record RegisterResponse(UUID id, String username, String email, Instant createdAt) {
}
