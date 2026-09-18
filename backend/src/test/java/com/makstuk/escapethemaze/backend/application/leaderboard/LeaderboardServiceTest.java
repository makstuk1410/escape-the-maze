package com.makstuk.escapethemaze.backend.application.leaderboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import com.makstuk.escapethemaze.backend.domain.game.GameStatus;
import com.makstuk.escapethemaze.backend.domain.maze.GeneratorType;
import com.makstuk.escapethemaze.backend.persistence.game.GameResult;
import com.makstuk.escapethemaze.backend.persistence.game.GameResultRepository;
import com.makstuk.escapethemaze.backend.persistence.user.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LeaderboardServiceTest {

    @Test
    void mapsTheDifficultySpecificRepositoryRankingToPublicEntries() {
        GameResultRepository gameResultRepository = mock(GameResultRepository.class);
        LeaderboardService service = new LeaderboardService(gameResultRepository);
        Instant now = Instant.parse("2026-09-18T12:00:00Z");
        GameResult first = result("first", 120, now);
        GameResult second = result("second", 100, now.plusSeconds(1));
        when(gameResultRepository.findLeaderboardByDifficulty(Difficulty.HARD)).thenReturn(List.of(first, second));

        LeaderboardService.Leaderboard leaderboard = service.getLeaderboard(Difficulty.HARD);

        assertThat(leaderboard.difficulty()).isEqualTo(Difficulty.HARD);
        assertThat(leaderboard.entries()).containsExactly(
                new LeaderboardService.Entry("first", 120, GameStatus.WON, now),
                new LeaderboardService.Entry("second", 100, GameStatus.WON, now.plusSeconds(1)));
        verify(gameResultRepository).findLeaderboardByDifficulty(Difficulty.HARD);
    }

    private GameResult result(String username, int score, Instant endedAt) {
        User user = new User(UUID.randomUUID(), username, username + "@example.com", "hash", endedAt.minusSeconds(60));
        return new GameResult(
                UUID.randomUUID(),
                UUID.randomUUID(),
                user,
                Difficulty.HARD,
                GeneratorType.KRUSKAL,
                score,
                GameStatus.WON,
                endedAt.minusSeconds(60),
                endedAt);
    }
}
