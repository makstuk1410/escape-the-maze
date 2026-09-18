package com.makstuk.escapethemaze.backend.domain.maze;

/**
 * A tile stored in the authoritative backend maze state.
 *
 * <p>Gameplay effects are applied by the game service, not by this enum.
 */
public enum TileType {
    WALL(false),
    EMPTY(true),
    GOLD(true),
    SPIKES(true),
    FREEZE(true),
    FOG(true),
    EXIT(true);

    private final boolean walkable;

    TileType(boolean walkable) {
        this.walkable = walkable;
    }

    public boolean isWalkable() {
        return walkable;
    }
}
