package com.makstuk.escapethemaze.backend.api.level;

import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the fixed, server-defined configuration for every leaderboard level.
 */
@RestController
@RequestMapping("/api/levels")
public class LevelController {

    @GetMapping
    public ResponseEntity<List<LevelResponse>> levels() {
        List<LevelResponse> levels = Arrays.stream(Difficulty.values())
                .map(LevelResponse::from)
                .toList();
        return ResponseEntity.ok(levels);
    }
}
