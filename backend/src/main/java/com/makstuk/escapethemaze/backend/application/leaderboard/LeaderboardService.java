package com.makstuk.escapethemaze.backend.application.leaderboard;

import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import com.makstuk.escapethemaze.backend.persistence.game.GameResult;
import com.makstuk.escapethemaze.backend.persistence.game.GameResultRepository;
import com.makstuk.escapethemaze.backend.domain.game.GameStatus;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Builds public, difficulty-specific rankings from persisted game results. */
@Service
public class LeaderboardService {

    private final GameResultRepository gameResultRepository;

    public LeaderboardService(GameResultRepository gameResultRepository) {
        this.gameResultRepository = gameResultRepository;
    }

    @Transactional(readOnly = true)
    public Leaderboard getLeaderboard(Difficulty difficulty) {
        Objects.requireNonNull(difficulty, "difficulty must not be null");
        List<Entry> entries = gameResultRepository.findLeaderboardByDifficulty(difficulty).stream()
                .map(this::toEntry)
                .toList();
        return new Leaderboard(difficulty, entries);
    }

    private Entry toEntry(GameResult result) {
        return new Entry(
                result.getUser().getUsername(), result.getScore(), result.getStatus(), result.getEndedAt());
    }

    public record Leaderboard(Difficulty difficulty, List<Entry> entries) {
        public Leaderboard {
            entries = List.copyOf(entries);
        }
    }

    public record Entry(String username, int score, GameStatus status, Instant endedAt) {
    }
}
