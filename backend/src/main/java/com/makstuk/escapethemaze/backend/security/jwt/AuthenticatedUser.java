package com.makstuk.escapethemaze.backend.security.jwt;

import java.util.UUID;

/**
 * The authenticated identity reconstructed from a verified JWT.
 */
public record AuthenticatedUser(UUID id, String username) {
}
