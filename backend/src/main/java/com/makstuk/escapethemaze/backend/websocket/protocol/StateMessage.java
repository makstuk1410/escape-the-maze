package com.makstuk.escapethemaze.backend.websocket.protocol;

import com.makstuk.escapethemaze.backend.domain.game.GameSession;
import com.makstuk.escapethemaze.backend.domain.game.GameStatus;
import com.makstuk.escapethemaze.backend.domain.maze.TileType;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Authoritative game-state update sent from the server to one WebSocket client. */
public record StateMessage(
        String type,
        long stateVersion,
        Player player,
        int score,
        GameStatus status,
        Instant endsAt,
        Effects effects,
        List<ChangedTile> changedTiles) {

    public StateMessage {
        if (!"STATE".equals(type)) {
            throw new IllegalArgumentException("type must be STATE");
        }
        player = Objects.requireNonNull(player, "player must not be null");
        status = Objects.requireNonNull(status, "status must not be null");
        endsAt = Objects.requireNonNull(endsAt, "endsAt must not be null");
        effects = Objects.requireNonNull(effects, "effects must not be null");
        changedTiles = List.copyOf(Objects.requireNonNull(changedTiles, "changedTiles must not be null"));
    }

    public static StateMessage from(GameSession session) {
        return from(session, List.of());
    }

    public static StateMessage from(GameSession session, List<ChangedTile> changedTiles) {
        Objects.requireNonNull(session, "session must not be null");
        return new StateMessage(
                "STATE",
                session.getStateVersion(),
                new Player(session.getPlayer().x(), session.getPlayer().y(), session.getPlayer().health()),
                session.getScore(),
                session.getStatus(),
                session.getEndsAt(),
                new Effects(session.getFrozenUntil(), session.getFogUntil()),
                changedTiles);
    }

    public record Player(int x, int y, int health) {
    }

    public record Effects(Instant frozenUntil, Instant fogUntil) {
    }

    public record ChangedTile(int x, int y, TileType type) {
        public ChangedTile {
            type = Objects.requireNonNull(type, "type must not be null");
        }
    }
}
