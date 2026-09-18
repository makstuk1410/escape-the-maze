package com.makstuk.escapethemaze.backend.application.game;

import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import com.makstuk.escapethemaze.backend.domain.game.GameSession;
import java.util.Optional;
import java.util.UUID;

/**
 * Application boundary for game creation, recovery, and command processing.
 */
public interface GameApplicationService {

    GameSession createGame(UUID ownerUserId, Difficulty difficulty);

    Optional<GameSession> findGame(UUID gameId);
}
