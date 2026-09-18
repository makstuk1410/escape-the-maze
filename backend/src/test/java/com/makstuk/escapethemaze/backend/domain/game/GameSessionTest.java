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

    @Test
    void collectsGoldOnceAndReplacesTheTileWithEmpty() {
        Instant startedAt = Instant.parse("2026-09-18T10:00:00Z");
        GameSession session = new GameSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Difficulty.NORMAL,
                goldMaze(),
                new PlayerState(1, 1),
                startedAt,
                startedAt.plusSeconds(300));

        session.updatePlayer(new PlayerState(1, 2));

        assertThat(session.collectGoldAtPlayer()).isTrue();
        assertThat(session.getScore()).isEqualTo(GameRules.GOLD_SCORE);
        assertThat(session.getMaze().tileAt(1, 2)).isEqualTo(TileType.EMPTY);
        assertThat(session.collectGoldAtPlayer()).isFalse();
        assertThat(session.getScore()).isEqualTo(GameRules.GOLD_SCORE);
        assertThat(session.getStateVersion()).isEqualTo(2);
    }

    @Test
    void appliesSpikeDamageOnlyAfterTheCooldownExpires() {
        Instant startedAt = Instant.parse("2026-09-18T10:00:00Z");
        GameSession session = new GameSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Difficulty.HARD,
                spikeMaze(),
                new PlayerState(1, 1),
                startedAt,
                startedAt.plusSeconds(300));

        assertThat(session.applySpikeDamageAtPlayer(startedAt)).isTrue();
        assertThat(session.getPlayer().health()).isEqualTo(80);
        assertThat(session.getDamageCooldownUntil()).isEqualTo(startedAt.plusSeconds(1));
        assertThat(session.applySpikeDamageAtPlayer(startedAt.plusMillis(500))).isFalse();
        assertThat(session.getPlayer().health()).isEqualTo(80);
        assertThat(session.applySpikeDamageAtPlayer(startedAt.plusSeconds(1))).isTrue();
        assertThat(session.getPlayer().health()).isEqualTo(60);
    }

    @Test
    void marksTheSessionLostWhenSpikeDamageDepletesHealth() {
        Instant startedAt = Instant.parse("2026-09-18T10:00:00Z");
        GameSession session = new GameSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Difficulty.HARD,
                spikeMaze(),
                new PlayerState(1, 1),
                startedAt,
                startedAt.plusSeconds(300));

        for (int damageNumber = 0; damageNumber < 5; damageNumber++) {
            assertThat(session.applySpikeDamageAtPlayer(startedAt.plusSeconds(damageNumber))).isTrue();
        }

        assertThat(session.getPlayer().health()).isZero();
        assertThat(session.getStatus()).isEqualTo(GameStatus.LOST);
        assertThat(session.applySpikeDamageAtPlayer(startedAt.plusSeconds(5))).isFalse();
    }

    @Test
    void setsAndExtendsTheFreezeDeadline() {
        Instant startedAt = Instant.parse("2026-09-18T10:00:00Z");
        GameSession session = new GameSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Difficulty.NORMAL,
                freezeMaze(),
                new PlayerState(1, 1),
                startedAt,
                startedAt.plusSeconds(300));

        assertThat(session.applyFreezeAtPlayer(startedAt)).isTrue();
        assertThat(session.getFrozenUntil()).isEqualTo(startedAt.plusSeconds(3));
        assertThat(session.isFrozenAt(startedAt.plusSeconds(2))).isTrue();

        assertThat(session.applyFreezeAtPlayer(startedAt.plusSeconds(1))).isTrue();
        assertThat(session.getFrozenUntil()).isEqualTo(startedAt.plusSeconds(4));
        assertThat(session.isFrozenAt(startedAt.plusSeconds(4))).isFalse();
    }

    @Test
    void setsAndExtendsTheFogDeadline() {
        Instant startedAt = Instant.parse("2026-09-18T10:00:00Z");
        GameSession session = new GameSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Difficulty.EXPERT,
                fogMaze(),
                new PlayerState(1, 1),
                startedAt,
                startedAt.plusSeconds(300));

        assertThat(session.applyFogAtPlayer(startedAt)).isTrue();
        assertThat(session.getFogUntil()).isEqualTo(startedAt.plusSeconds(4));
        assertThat(session.isFogActiveAt(startedAt.plusSeconds(3))).isTrue();

        assertThat(session.applyFogAtPlayer(startedAt.plusSeconds(2))).isTrue();
        assertThat(session.getFogUntil()).isEqualTo(startedAt.plusSeconds(6));
        assertThat(session.isFogActiveAt(startedAt.plusSeconds(6))).isFalse();
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

    private Maze goldMaze() {
        TileType[][] tiles = {
            {TileType.WALL, TileType.WALL, TileType.WALL},
            {TileType.WALL, TileType.EMPTY, TileType.WALL},
            {TileType.WALL, TileType.GOLD, TileType.WALL},
            {TileType.WALL, TileType.EXIT, TileType.WALL}
        };
        return new Maze(tiles, new Position(1, 1), new Position(1, 3), GeneratorType.PRIM);
    }

    private Maze spikeMaze() {
        TileType[][] tiles = {
            {TileType.WALL, TileType.WALL, TileType.WALL},
            {TileType.WALL, TileType.SPIKES, TileType.WALL},
            {TileType.WALL, TileType.EXIT, TileType.WALL}
        };
        return new Maze(tiles, new Position(1, 1), new Position(1, 2), GeneratorType.KRUSKAL);
    }

    private Maze freezeMaze() {
        TileType[][] tiles = {
            {TileType.WALL, TileType.WALL, TileType.WALL},
            {TileType.WALL, TileType.FREEZE, TileType.WALL},
            {TileType.WALL, TileType.EXIT, TileType.WALL}
        };
        return new Maze(tiles, new Position(1, 1), new Position(1, 2), GeneratorType.BINARY_TREE);
    }

    private Maze fogMaze() {
        TileType[][] tiles = {
            {TileType.WALL, TileType.WALL, TileType.WALL},
            {TileType.WALL, TileType.FOG, TileType.WALL},
            {TileType.WALL, TileType.EXIT, TileType.WALL}
        };
        return new Maze(tiles, new Position(1, 1), new Position(1, 2), GeneratorType.PRIM);
    }
}
