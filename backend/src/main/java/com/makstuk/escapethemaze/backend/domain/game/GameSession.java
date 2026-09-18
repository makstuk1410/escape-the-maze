package com.makstuk.escapethemaze.backend.domain.game;

import java.util.UUID;

/**
 * Server-authoritative in-memory game state. Persistence is intentionally
 * limited to completed results; this type is a migration placeholder.
 */
public record GameSession(UUID id, UUID userId, Difficulty difficulty, GameStatus status) {
}
