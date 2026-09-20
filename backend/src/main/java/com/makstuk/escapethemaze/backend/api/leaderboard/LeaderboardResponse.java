package com.makstuk.escapethemaze.backend.api.leaderboard;

import com.makstuk.escapethemaze.backend.application.leaderboard.LeaderboardService;
import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import com.makstuk.escapethemaze.backend.domain.game.GameStatus;
import java.time.Instant;
import java.util.List;

/** Public API representation of one difficulty leaderboard. */
public record LeaderboardResponse(Difficulty difficulty, List<Entry> entries) {

    static LeaderboardResponse from(LeaderboardService.Leaderboard leaderboard) {
        List<Entry> entries = new java.util.ArrayList<>();
        for (int index = 0; index < leaderboard.entries().size(); index++) {
            LeaderboardService.Entry entry = leaderboard.entries().get(index);
            entries.add(new Entry(index + 1, entry.username(), entry.score(), entry.status(), entry.endedAt()));
        }
        return new LeaderboardResponse(leaderboard.difficulty(), entries);
    }

    public record Entry(int rank, String username, int score, GameStatus status, Instant endedAt) {
    }
}
