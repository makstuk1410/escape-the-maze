package com.makstuk.escapethemaze.backend.persistence.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import com.makstuk.escapethemaze.backend.domain.game.GameStatus;
import com.makstuk.escapethemaze.backend.domain.maze.GeneratorType;
import com.makstuk.escapethemaze.backend.persistence.user.User;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GameResultTest {

    @Test
    void storesACompletedServerCalculatedResult() {
        Instant startedAt = Instant.parse("2026-09-18T12:00:00Z");
        User user = new User(UUID.randomUUID(), "maze_tester", "tester@example.com", "hash", startedAt);

        GameResult result = new GameResult(
                UUID.randomUUID(),
                UUID.randomUUID(),
                user,
                Difficulty.HARD,
                GeneratorType.KRUSKAL,
                120,
                GameStatus.WON,
                startedAt,
                startedAt.plusSeconds(90));

        assertThat(result.getUser()).isSameAs(user);
        assertThat(result.getDifficulty()).isEqualTo(Difficulty.HARD);
        assertThat(result.getGenerator()).isEqualTo(GeneratorType.KRUSKAL);
        assertThat(result.getScore()).isEqualTo(120);
        assertThat(result.getStatus()).isEqualTo(GameStatus.WON);
    }

    @Test
    void rejectsNonTerminalStatusesAndNegativeScores() {
        Instant now = Instant.parse("2026-09-18T12:00:00Z");
        User user = new User(UUID.randomUUID(), "maze_tester", "tester@example.com", "hash", now);

        assertThatThrownBy(() -> new GameResult(
                        UUID.randomUUID(), UUID.randomUUID(), user, Difficulty.EASY, GeneratorType.DFS,
                        0, GameStatus.RUNNING, now, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("terminal");
        assertThatThrownBy(() -> new GameResult(
                        UUID.randomUUID(), UUID.randomUUID(), user, Difficulty.EASY, GeneratorType.DFS,
                        -1, GameStatus.LOST, now, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }
}
