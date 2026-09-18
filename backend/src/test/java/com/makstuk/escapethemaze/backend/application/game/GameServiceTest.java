package com.makstuk.escapethemaze.backend.application.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import com.makstuk.escapethemaze.backend.domain.game.Direction;
import com.makstuk.escapethemaze.backend.domain.game.GameRules;
import com.makstuk.escapethemaze.backend.domain.game.GameSession;
import com.makstuk.escapethemaze.backend.domain.game.GameStatus;
import com.makstuk.escapethemaze.backend.domain.maze.TileType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class GameServiceTest {

    @Test
    void createsAndRetainsAConfiguredSessionForEachDifficulty() {
        Instant now = Instant.parse("2026-09-18T12:00:00Z");
        GameService gameService = new GameService(Clock.fixed(now, ZoneOffset.UTC), new Random(42));
        UUID ownerUserId = UUID.randomUUID();

        for (Difficulty difficulty : Difficulty.values()) {
            var session = gameService.createGame(ownerUserId, difficulty);

            assertThat(session.getOwnerUserId()).isEqualTo(ownerUserId);
            assertThat(session.getDifficulty()).isEqualTo(difficulty);
            assertThat(session.getStatus()).isEqualTo(GameStatus.RUNNING);
            assertThat(session.getStartedAt()).isEqualTo(now);
            assertThat(session.getEndsAt()).isEqualTo(now.plus(GameRules.GAME_DURATION));
            assertThat(session.getMaze().getGeneratorType()).isEqualTo(GameRules.generatorFor(difficulty));
            assertThat(session.getMaze().getWidth()).isEqualTo(2 * GameRules.mazeWidthFor(difficulty) + 1);
            assertThat(session.getMaze().getHeight()).isEqualTo(2 * GameRules.mazeHeightFor(difficulty) + 1);
            assertThat(session.getPlayer().x()).isEqualTo(session.getMaze().getStart().x());
            assertThat(session.getPlayer().y()).isEqualTo(session.getMaze().getStart().y());
            assertThat(session.getMaze().tileAt(
                    session.getMaze().getExit().x(), session.getMaze().getExit().y())).isEqualTo(TileType.EXIT);
            assertThat(gameService.findGame(session.getId())).containsSame(session);
        }
    }

    @Test
    void movesIntoGoldAndAppliesTheGoldRule() {
        GameService gameService = gameService();
        UUID ownerUserId = UUID.randomUUID();
        var session = gameService.createGame(ownerUserId, Difficulty.EASY);
        int targetX = session.getPlayer().x();
        int targetY = session.getPlayer().y() + 1;
        session.getMaze().replaceTile(targetX, targetY, TileType.GOLD);

        var result = gameService.move(session.getId(), ownerUserId, Direction.DOWN);

        assertThat(result).isSameAs(session);
        assertThat(result.getPlayer()).hasFieldOrPropertyWithValue("x", targetX)
                .hasFieldOrPropertyWithValue("y", targetY);
        assertThat(result.getScore()).isEqualTo(GameRules.GOLD_SCORE);
        assertThat(result.getMaze().tileAt(targetX, targetY)).isEqualTo(TileType.EMPTY);
    }

    @Test
    void rejectsMovementIntoAWallWithoutChangingThePlayer() {
        GameService gameService = gameService();
        UUID ownerUserId = UUID.randomUUID();
        var session = gameService.createGame(ownerUserId, Difficulty.EASY);
        var initialPlayer = session.getPlayer();
        int targetX = session.getPlayer().x();
        int targetY = session.getPlayer().y() + 1;
        session.getMaze().replaceTile(targetX, targetY, TileType.WALL);

        var result = gameService.move(session.getId(), ownerUserId, Direction.DOWN);

        assertThat(result.getPlayer()).isEqualTo(initialPlayer);
        assertThat(result.getScore()).isZero();
    }

    @Test
    void rejectsCommandsForAnotherUsersSession() {
        GameService gameService = gameService();
        var session = gameService.createGame(UUID.randomUUID(), Difficulty.EASY);

        assertThatThrownBy(() -> gameService.move(session.getId(), UUID.randomUUID(), Direction.DOWN))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("do not own");
    }

    @Test
    void returnsOnlyTheOwnersGameSession() {
        GameService gameService = gameService();
        UUID ownerUserId = UUID.randomUUID();
        var session = gameService.createGame(ownerUserId, Difficulty.EASY);

        assertThat(gameService.getGame(session.getId(), ownerUserId)).isSameAs(session);
        assertThatThrownBy(() -> gameService.getGame(session.getId(), UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("do not own");
    }

    @Test
    void appliesSpikeDamageAfterMovingOntoSpikes() {
        GameService gameService = gameService();
        UUID ownerUserId = UUID.randomUUID();
        var session = gameService.createGame(ownerUserId, Difficulty.EASY);
        makeDownTile(session, TileType.SPIKES);

        var result = gameService.move(session.getId(), ownerUserId, Direction.DOWN);

        assertThat(result.getPlayer().health()).isEqualTo(100 - GameRules.SPIKES_DAMAGE);
        assertThat(result.getDamageCooldownUntil()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void freezesFurtherMovementAfterMovingOntoFreeze() {
        GameService gameService = gameService();
        UUID ownerUserId = UUID.randomUUID();
        var session = gameService.createGame(ownerUserId, Difficulty.EASY);
        makeDownTile(session, TileType.FREEZE);

        var frozenSession = gameService.move(session.getId(), ownerUserId, Direction.DOWN);
        var frozenPlayer = frozenSession.getPlayer();
        var result = gameService.move(session.getId(), ownerUserId, Direction.RIGHT);

        assertThat(frozenSession.getFrozenUntil()).isNotNull();
        assertThat(result.getPlayer()).isEqualTo(frozenPlayer);
    }

    @Test
    void activatesFogAfterMovingOntoFog() {
        GameService gameService = gameService();
        UUID ownerUserId = UUID.randomUUID();
        var session = gameService.createGame(ownerUserId, Difficulty.EASY);
        makeDownTile(session, TileType.FOG);

        var result = gameService.move(session.getId(), ownerUserId, Direction.DOWN);

        assertThat(result.getFogUntil()).isNotNull();
        assertThat(result.isFogActiveAt(Instant.parse("2026-09-18T12:00:00Z"))).isTrue();
    }

    @Test
    void winsAndAwardsExitScoreAfterMovingOntoExit() {
        GameService gameService = gameService();
        UUID ownerUserId = UUID.randomUUID();
        var session = gameService.createGame(ownerUserId, Difficulty.EASY);
        makeDownTile(session, TileType.EXIT);

        var result = gameService.move(session.getId(), ownerUserId, Direction.DOWN);

        assertThat(result.getStatus()).isEqualTo(GameStatus.WON);
        assertThat(result.getScore()).isEqualTo(GameRules.EXIT_SCORE);
        assertThat(result.acceptsMovement()).isFalse();
    }

    private void makeDownTile(GameSession session, TileType tileType) {
        session.getMaze().replaceTile(session.getPlayer().x(), session.getPlayer().y() + 1, tileType);
    }

    private GameService gameService() {
        return new GameService(
                Clock.fixed(Instant.parse("2026-09-18T12:00:00Z"), ZoneOffset.UTC), new Random(42));
    }
}
