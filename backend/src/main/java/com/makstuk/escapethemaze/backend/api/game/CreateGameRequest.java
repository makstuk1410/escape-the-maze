package com.makstuk.escapethemaze.backend.api.game;

import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import jakarta.validation.constraints.NotNull;

/** Request to create one server-owned game session. */
public record CreateGameRequest(@NotNull Difficulty difficulty) {
}
