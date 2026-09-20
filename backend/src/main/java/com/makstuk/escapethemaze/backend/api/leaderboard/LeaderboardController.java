package com.makstuk.escapethemaze.backend.api.leaderboard;

import com.makstuk.escapethemaze.backend.application.leaderboard.LeaderboardService;
import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Public read-only endpoint for one difficulty's verified ranking. */
@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public ResponseEntity<LeaderboardResponse> getLeaderboard(@RequestParam Difficulty difficulty) {
        return ResponseEntity.ok(LeaderboardResponse.from(leaderboardService.getLeaderboard(difficulty)));
    }
}
