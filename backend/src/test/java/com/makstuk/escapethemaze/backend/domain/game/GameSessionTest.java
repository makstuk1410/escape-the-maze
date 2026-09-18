package com.makstuk.escapethemaze.backend.domain.game;

import static org.assertj.core.api.Assertions.assertThat;

import com.makstuk.escapethemaze.backend.domain.maze.GeneratorType;
import com.makstuk.escapethemaze.backend.domain.maze.Maze;
import com.makstuk.escapethemaze.backend.domain.maze.Position;
import com.makstuk.escapethemaze.backend.domain.maze.TileType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GameSessionTest {

    @Test
    void startsRunningAndVersionsEachStateUpdate() {
        Instant startedAt = Instant.parse("2026-09-18T10:00:00Z");
        GameSession session = new GameSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Difficulty.EASY,
                maze(),
                new PlayerState(1, 1),
                startedAt,
                startedAt.plusSeconds(300));

        assertThat(session.getStatus()).isEqualTo(GameStatus.RUNNING);
        assertThat(session.getScore()).isZero();
        assertThat(session.getStateVersion()).isZero();
        assertThat(session.acceptsMovement()).isTrue();

        session.updatePlayer(new PlayerState(1, 2));
        session.addScore(50);
        session.updateStatus(GameStatus.PAUSED);

        assertThat(session.getPlayer()).isEqualTo(new PlayerState(1, 2));
        assertThat(session.getScore()).isEqualTo(50);
        assertThat(session.acceptsMovement()).isFalse();
        assertThat(session.getStateVersion()).isEqualTo(3);
    }

    @Test
    void rejectsBoundaryAndWallMovesButReturnsAWalkableTarget() {
        Instant startedAt = Instant.parse("2026-09-18T10:00:00Z");
        GameSession session = new GameSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Difficulty.EASY,
                boundaryMaze(),
                new PlayerState(0, 0),
                startedAt,
                startedAt.plusSeconds(300));

        assertThat(session.validMoveTarget(Direction.UP)).isEmpty();
        assertThat(session.validMoveTarget(Direction.LEFT)).isEmpty();
        assertThat(session.validMoveTarget(Direction.RIGHT)).isEmpty();
        assertThat(session.validMoveTarget(Direction.DOWN)).contains(new Position(0, 1));
    }

    private Maze maze() {
        TileType[][] tiles = {
            {TileType.WALL, TileType.WALL, TileType.WALL},
            {TileType.WALL, TileType.EMPTY, TileType.WALL},
            {TileType.WALL, TileType.EXIT, TileType.WALL}
        };
        return new Maze(tiles, new Position(1, 1), new Position(1, 2), GeneratorType.DFS);
    }

    private Maze boundaryMaze() {
        TileType[][] tiles = {
            {TileType.EMPTY, TileType.WALL, TileType.WALL},
            {TileType.EMPTY, TileType.EMPTY, TileType.WALL},
            {TileType.WALL, TileType.EXIT, TileType.WALL}
        };
        return new Maze(tiles, new Position(0, 0), new Position(1, 2), GeneratorType.DFS);
    }
}
