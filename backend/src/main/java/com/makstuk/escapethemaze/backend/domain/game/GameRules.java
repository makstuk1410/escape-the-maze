package com.makstuk.escapethemaze.backend.domain.game;

import com.makstuk.escapethemaze.backend.domain.maze.GeneratorType;
import java.time.Duration;

/**
 * Backend gameplay values migrated from the JavaFX game configuration.
 */
public final class GameRules {

    public static final int GOLD_SCORE = 10;
    public static final int SPIKES_DAMAGE = 20;
    public static final Duration DAMAGE_COOLDOWN = Duration.ofSeconds(1);
    public static final Duration FREEZE_DURATION = Duration.ofSeconds(3);
    public static final Duration FOG_DURATION = Duration.ofSeconds(4);
    public static final int EXIT_SCORE = 100;
    public static final Duration GAME_DURATION = Duration.ofMinutes(5);

    private GameRules() {
    }

    public static GeneratorType generatorFor(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> GeneratorType.DFS;
            case NORMAL -> GeneratorType.PRIM;
            case HARD, EXPERT -> GeneratorType.KRUSKAL;
        };
    }

    public static int mazeWidthFor(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 15;
            case NORMAL, HARD -> 20;
            case EXPERT -> 30;
        };
    }

    public static int mazeHeightFor(Difficulty difficulty) {
        return mazeWidthFor(difficulty);
    }

    /** Number of non-exit special tiles placed in each newly generated maze. */
    public static SpecialTileCounts specialTileCountsFor(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> new SpecialTileCounts(8, 3, 1, 1);
            case NORMAL -> new SpecialTileCounts(14, 7, 3, 3);
            case HARD -> new SpecialTileCounts(22, 12, 5, 5);
            case EXPERT -> new SpecialTileCounts(35, 20, 9, 9);
        };
    }

    public record SpecialTileCounts(int gold, int spikes, int freeze, int fog) {
    }
}
