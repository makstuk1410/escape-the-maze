package com.makstuk.escapethemaze.backend.api.level;

import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import com.makstuk.escapethemaze.backend.domain.game.GameRules;
import com.makstuk.escapethemaze.backend.domain.maze.GeneratorType;

/**
 * A supported leaderboard level and its server-defined configuration.
 */
public record LevelResponse(
        Difficulty difficulty,
        GeneratorType generator,
        int logicalMazeWidth,
        int logicalMazeHeight,
        int gridWidth,
        int gridHeight,
        long timeLimitSeconds) {

    static LevelResponse from(Difficulty difficulty) {
        int logicalWidth = GameRules.mazeWidthFor(difficulty);
        int logicalHeight = GameRules.mazeHeightFor(difficulty);
        return new LevelResponse(
                difficulty,
                GameRules.generatorFor(difficulty),
                logicalWidth,
                logicalHeight,
                2 * logicalWidth + 1,
                2 * logicalHeight + 1,
                GameRules.GAME_DURATION.toSeconds());
    }
}
