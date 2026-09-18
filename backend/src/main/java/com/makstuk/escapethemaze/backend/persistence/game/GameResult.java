package com.makstuk.escapethemaze.backend.persistence.game;

import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import com.makstuk.escapethemaze.backend.domain.game.GameStatus;
import com.makstuk.escapethemaze.backend.domain.maze.GeneratorType;
import com.makstuk.escapethemaze.backend.persistence.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** A durable, server-calculated record of one completed game session. */
@Entity
@Table(name = "game_results")
public class GameResult {

    @Id
    private UUID id;

    @Column(name = "game_id", nullable = false, unique = true)
    private UUID gameId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private GeneratorType generator;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private GameStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at", nullable = false)
    private Instant endedAt;

    protected GameResult() {
        // Required by JPA.
    }

    public GameResult(
            UUID id,
            UUID gameId,
            User user,
            Difficulty difficulty,
            GeneratorType generator,
            int score,
            GameStatus status,
            Instant startedAt,
            Instant endedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.gameId = Objects.requireNonNull(gameId, "gameId must not be null");
        this.user = Objects.requireNonNull(user, "user must not be null");
        this.difficulty = Objects.requireNonNull(difficulty, "difficulty must not be null");
        this.generator = Objects.requireNonNull(generator, "generator must not be null");
        if (score < 0) {
            throw new IllegalArgumentException("score must not be negative");
        }
        this.score = score;
        this.status = Objects.requireNonNull(status, "status must not be null");
        if (!isTerminal(status)) {
            throw new IllegalArgumentException("status must be terminal");
        }
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt must not be null");
        this.endedAt = Objects.requireNonNull(endedAt, "endedAt must not be null");
        if (endedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("endedAt must not be before startedAt");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getGameId() {
        return gameId;
    }

    public User getUser() {
        return user;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public GeneratorType getGenerator() {
        return generator;
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

    public Instant getEndedAt() {
        return endedAt;
    }

    private boolean isTerminal(GameStatus status) {
        return status == GameStatus.WON || status == GameStatus.LOST || status == GameStatus.TIMED_OUT;
    }
}
