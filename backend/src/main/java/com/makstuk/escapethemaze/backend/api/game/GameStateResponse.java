package com.makstuk.escapethemaze.backend.api.game;

import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import com.makstuk.escapethemaze.backend.domain.game.GameSession;
import com.makstuk.escapethemaze.backend.domain.game.GameStatus;
import com.makstuk.escapethemaze.backend.domain.maze.GeneratorType;
import com.makstuk.escapethemaze.backend.domain.maze.TileType;
import java.time.Instant;
import java.util.UUID;

/** Frontend-safe snapshot of the authoritative game state. */
public record GameStateResponse(
        UUID gameId,
        Difficulty difficulty,
        GeneratorType generator,
        int width,
        int height,
        TileType[][] tiles,
        PlayerResponse player,
        int score,
        GameStatus status,
        Instant startedAt,
        Instant endsAt,
        long stateVersion) {

    static GameStateResponse from(GameSession session) {
        return new GameStateResponse(
                session.getId(),
                session.getDifficulty(),
                session.getMaze().getGeneratorType(),
                session.getMaze().getWidth(),
                session.getMaze().getHeight(),
                session.getMaze().getTiles(),
                new PlayerResponse(session.getPlayer().x(), session.getPlayer().y(), session.getPlayer().health()),
                session.getScore(),
                session.getStatus(),
                session.getStartedAt(),
                session.getEndsAt(),
                session.getStateVersion());
    }

    public record PlayerResponse(int x, int y, int health) {
    }
}
