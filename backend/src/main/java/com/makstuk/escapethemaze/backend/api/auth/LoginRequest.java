package com.makstuk.escapethemaze.backend.api.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Validated credentials accepted when an existing user logs in.
 */
public record LoginRequest(
        @NotBlank(message = "Email or username is required")
        @Size(max = 255, message = "Email or username must be at most 255 characters")
        String identifier,

        @NotBlank(message = "Password is required")
        @Size(max = 72, message = "Password must be at most 72 characters")
        String password) {
}
