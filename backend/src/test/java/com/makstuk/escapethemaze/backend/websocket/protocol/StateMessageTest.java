package com.makstuk.escapethemaze.backend.websocket.protocol;

import static org.assertj.core.api.Assertions.assertThat;

import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import com.makstuk.escapethemaze.backend.domain.game.GameSession;
import com.makstuk.escapethemaze.backend.domain.game.PlayerState;
import com.makstuk.escapethemaze.backend.domain.maze.GeneratorType;
import com.makstuk.escapethemaze.backend.domain.maze.Maze;
import com.makstuk.escapethemaze.backend.domain.maze.Position;
import com.makstuk.escapethemaze.backend.domain.maze.TileType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StateMessageTest {

    @Test
    void mapsAuthoritativeSessionDataAndChangedTiles() {
        Instant startedAt = Instant.parse("2026-09-18T12:00:00Z");
        GameSession session = session(startedAt);
        session.addScore(10);
        session.updateFrozenUntil(startedAt.plusSeconds(3));
        session.updateFogUntil(startedAt.plusSeconds(4));
        var changedTile = new StateMessage.ChangedTile(1, 1, TileType.EMPTY);

        StateMessage message = StateMessage.from(session, List.of(changedTile));

        assertThat(message.type()).isEqualTo("STATE");
        assertThat(message.stateVersion()).isEqualTo(3);
        assertThat(message.player()).isEqualTo(new StateMessage.Player(1, 1, 100));
        assertThat(message.score()).isEqualTo(10);
        assertThat(message.status()).isEqualTo(session.getStatus());
        assertThat(message.endsAt()).isEqualTo(startedAt.plusSeconds(300));
        assertThat(message.effects()).isEqualTo(new StateMessage.Effects(
                startedAt.plusSeconds(3), startedAt.plusSeconds(4)));
        assertThat(message.changedTiles()).containsExactly(changedTile);
    }

    private GameSession session(Instant startedAt) {
        TileType[][] tiles = {
            {TileType.WALL, TileType.WALL, TileType.WALL},
            {TileType.WALL, TileType.EMPTY, TileType.WALL},
            {TileType.WALL, TileType.EXIT, TileType.WALL}
        };
        Maze maze = new Maze(tiles, new Position(1, 1), new Position(1, 2), GeneratorType.DFS);
        return new GameSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Difficulty.EASY,
                maze,
                new PlayerState(1, 1),
                startedAt,
                startedAt.plusSeconds(300));
    }
}
