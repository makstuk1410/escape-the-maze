package com.makstuk.escapethemaze.backend.security.jwt;

import java.time.Instant;

/**
 * A signed access token and the moment when it becomes invalid.
 */
public record GeneratedToken(String value, Instant expiresAt) {
}
