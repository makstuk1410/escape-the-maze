package com.makstuk.escapethemaze.backend.api.auth;

import java.time.Instant;
import java.util.UUID;

/**
 * Safe profile data for the user identified by a verified access token.
 */
public record CurrentUserResponse(UUID id, String username, String email, Instant createdAt) {
}
