package com.makstuk.escapethemaze.backend.domain.game;

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

    private GameRules() {
    }
}
