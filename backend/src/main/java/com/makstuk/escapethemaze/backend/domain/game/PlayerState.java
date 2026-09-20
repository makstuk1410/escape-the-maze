package com.makstuk.escapethemaze.backend.domain.game;

/**
 * Immutable, server-authoritative player state in maze grid coordinates.
 */
public record PlayerState(int x, int y, int health) {

    public static final int INITIAL_HEALTH = 100;

    public PlayerState {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Player coordinates must not be negative");
        }
        if (health < 0) {
            throw new IllegalArgumentException("Player health must not be negative");
        }
    }

    public PlayerState(int x, int y) {
        this(x, y, INITIAL_HEALTH);
    }

    public PlayerState moveTo(int targetX, int targetY) {
        return new PlayerState(targetX, targetY, health);
    }

    public PlayerState withHealth(int newHealth) {
        return new PlayerState(x, y, newHealth);
    }
}
