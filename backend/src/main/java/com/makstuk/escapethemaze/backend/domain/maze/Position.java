package com.makstuk.escapethemaze.backend.domain.maze;

/**
 * Immutable coordinate in a maze tile grid.
 */
public record Position(int x, int y) {

    public Position {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Position coordinates must not be negative");
        }
    }
}
