package com.makstuk.escapethemaze.backend.domain.game;

import com.makstuk.escapethemaze.backend.domain.maze.Maze;
import com.makstuk.escapethemaze.backend.domain.maze.Position;
import com.makstuk.escapethemaze.backend.domain.maze.TileType;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-authoritative in-memory state for one active game.
 */
public class GameSession {

    private final UUID id;
    private final UUID ownerUserId;
    private final Difficulty difficulty;
    private final Maze maze;
    private PlayerState player;
    private int score;
    private GameStatus status;
    private final Instant startedAt;
    private final Instant endsAt;
    private Instant frozenUntil;
    private Instant fogUntil;
    private Instant damageCooldownUntil;
    private long stateVersion;

    public GameSession(
            UUID id,
            UUID ownerUserId,
            Difficulty difficulty,
            Maze maze,
            PlayerState player,
            Instant startedAt,
            Instant endsAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.ownerUserId = Objects.requireNonNull(ownerUserId, "ownerUserId must not be null");
        this.difficulty = Objects.requireNonNull(difficulty, "difficulty must not be null");
        this.maze = Objects.requireNonNull(maze, "maze must not be null");
        this.player = Objects.requireNonNull(player, "player must not be null");
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt must not be null");
        this.endsAt = Objects.requireNonNull(endsAt, "endsAt must not be null");
        if (!endsAt.isAfter(startedAt)) {
            throw new IllegalArgumentException("endsAt must be after startedAt");
        }
        if (!maze.isInBounds(player.x(), player.y()) || !maze.tileAt(player.x(), player.y()).isWalkable()) {
            throw new IllegalArgumentException("Player must start on a walkable maze tile");
        }
        this.score = 0;
        this.status = GameStatus.RUNNING;
        this.stateVersion = 0;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Maze getMaze() {
        return maze;
    }

    public PlayerState getPlayer() {
        return player;
    }

    public int getScore() {
        return score;
    }

    public GameStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public Instant getFrozenUntil() {
        return frozenUntil;
    }

    public Instant getFogUntil() {
        return fogUntil;
    }

    public Instant getDamageCooldownUntil() {
        return damageCooldownUntil;
    }

    public long getStateVersion() {
        return stateVersion;
    }

    public boolean acceptsMovement() {
        return status == GameStatus.RUNNING;
    }

    /**
     * Returns the one-tile target only when it is inside the maze and walkable.
     * This method does not mutate player state.
     */
    public Optional<Position> validMoveTarget(Direction direction) {
        Objects.requireNonNull(direction, "direction must not be null");
        if (!acceptsMovement()) {
            return Optional.empty();
        }

        int targetX = player.x() + direction.deltaX();
        int targetY = player.y() + direction.deltaY();
        if (!maze.isInBounds(targetX, targetY) || !maze.tileAt(targetX, targetY).isWalkable()) {
            return Optional.empty();
        }
        return Optional.of(new Position(targetX, targetY));
    }

    public void updatePlayer(PlayerState player) {
        this.player = Objects.requireNonNull(player, "player must not be null");
        incrementStateVersion();
    }

    public void addScore(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Score points must not be negative");
        }
        score += points;
        incrementStateVersion();
    }

    /**
     * Collects gold under the current player position exactly once.
     *
     * @return {@code true} when gold was collected; otherwise {@code false}
     */
    public boolean collectGoldAtPlayer() {
        if (!acceptsMovement() || maze.tileAt(player.x(), player.y()) != TileType.GOLD) {
            return false;
        }

        maze.replaceTile(player.x(), player.y(), TileType.EMPTY);
        score += GameRules.GOLD_SCORE;
        incrementStateVersion();
        return true;
    }

    /**
     * Applies spike damage at the current position, respecting the damage cooldown.
     *
     * @return {@code true} when damage was applied; otherwise {@code false}
     */
    public boolean applySpikeDamageAtPlayer(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        if (!acceptsMovement()
                || maze.tileAt(player.x(), player.y()) != TileType.SPIKES
                || (damageCooldownUntil != null && now.isBefore(damageCooldownUntil))) {
            return false;
        }

        int remainingHealth = Math.max(0, player.health() - GameRules.SPIKES_DAMAGE);
        player = player.withHealth(remainingHealth);
        damageCooldownUntil = now.plus(GameRules.DAMAGE_COOLDOWN);
        if (remainingHealth == 0) {
            status = GameStatus.LOST;
        }
        incrementStateVersion();
        return true;
    }

    public boolean applySpikeDamageAtPlayer() {
        return applySpikeDamageAtPlayer(Instant.now());
    }

    public void updateStatus(GameStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
        incrementStateVersion();
    }

    public void updateFrozenUntil(Instant frozenUntil) {
        this.frozenUntil = frozenUntil;
        incrementStateVersion();
    }

    public void updateFogUntil(Instant fogUntil) {
        this.fogUntil = fogUntil;
        incrementStateVersion();
    }

    private void incrementStateVersion() {
        stateVersion++;
    }
}
