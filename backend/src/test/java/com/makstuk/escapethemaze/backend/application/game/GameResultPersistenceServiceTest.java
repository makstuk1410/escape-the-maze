package com.makstuk.escapethemaze.backend.application.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import com.makstuk.escapethemaze.backend.domain.game.GameSession;
import com.makstuk.escapethemaze.backend.domain.game.GameStatus;
import com.makstuk.escapethemaze.backend.domain.game.PlayerState;
import com.makstuk.escapethemaze.backend.domain.maze.GeneratorType;
import com.makstuk.escapethemaze.backend.domain.maze.Maze;
import com.makstuk.escapethemaze.backend.domain.maze.Position;
import com.makstuk.escapethemaze.backend.domain.maze.TileType;
import com.makstuk.escapethemaze.backend.persistence.game.GameResult;
import com.makstuk.escapethemaze.backend.persistence.game.GameResultRepository;
import com.makstuk.escapethemaze.backend.persistence.user.User;
import com.makstuk.escapethemaze.backend.persistence.user.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GameResultPersistenceServiceTest {

    @Test
    void persistsTheWonServerStateOnce() {
        GameResultRepository gameResultRepository = mock(GameResultRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        GameResultPersistenceService service = new GameResultPersistenceService(gameResultRepository, userRepository);
        Instant startedAt = Instant.parse("2026-09-18T12:00:00Z");
        Instant endedAt = startedAt.plusSeconds(90);
        GameSession session = terminalSession(startedAt);
        User user = new User(session.getOwnerUserId(), "maze_tester", "tester@example.com", "hash", startedAt);
        when(gameResultRepository.existsByGameId(session.getId())).thenReturn(false);
        when(userRepository.findById(session.getOwnerUserId())).thenReturn(Optional.of(user));

        service.persistIfCompleted(session, endedAt);

        ArgumentCaptor<GameResult> savedResult = ArgumentCaptor.forClass(GameResult.class);
        verify(gameResultRepository).saveAndFlush(savedResult.capture());
        assertThat(savedResult.getValue().getGameId()).isEqualTo(session.getId());
        assertThat(savedResult.getValue().getUser()).isSameAs(user);
        assertThat(savedResult.getValue().getScore()).isEqualTo(session.getScore());
        assertThat(savedResult.getValue().getStatus()).isEqualTo(GameStatus.WON);
        assertThat(savedResult.getValue().getEndedAt()).isEqualTo(endedAt);
    }

    @Test
    void doesNotPersistAnAlreadySavedOrNonWinningSession() {
        GameResultRepository gameResultRepository = mock(GameResultRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        GameResultPersistenceService service = new GameResultPersistenceService(gameResultRepository, userRepository);
        Instant now = Instant.parse("2026-09-18T12:00:00Z");
        GameSession completedSession = terminalSession(now);
        when(gameResultRepository.existsByGameId(completedSession.getId())).thenReturn(true);

        service.persistIfCompleted(completedSession, now.plusSeconds(1));
        service.persistIfCompleted(runningSession(now), now.plusSeconds(1));

        GameSession lostSession = runningSession(now);
        lostSession.updateStatus(GameStatus.LOST);
        GameSession timedOutSession = runningSession(now);
        timedOutSession.updateStatus(GameStatus.TIMED_OUT);
        service.persistIfCompleted(lostSession, now.plusSeconds(1));
        service.persistIfCompleted(timedOutSession, now.plusSeconds(1));

        verify(gameResultRepository).existsByGameId(completedSession.getId());
        verifyNoInteractions(userRepository);
        verify(gameResultRepository, org.mockito.Mockito.never()).saveAndFlush(any());
    }

    private GameSession terminalSession(Instant startedAt) {
        GameSession session = runningSession(startedAt);
        session.updateStatus(GameStatus.WON);
        return session;
    }

    private GameSession runningSession(Instant startedAt) {
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
