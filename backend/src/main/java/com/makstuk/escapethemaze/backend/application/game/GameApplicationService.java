package com.makstuk.escapethemaze.backend.application.game;

import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import com.makstuk.escapethemaze.backend.domain.game.Direction;
import com.makstuk.escapethemaze.backend.domain.game.GameSession;
import java.util.UUID;

/**
 * Application boundary for game creation, recovery, and command processing.
 */
public interface GameApplicationService {

    GameSession createGame(UUID ownerUserId, Difficulty difficulty);

    /**
     * Applies one player movement command and returns the resulting authoritative state.
     */
    GameSession move(UUID gameId, UUID ownerUserId, Direction direction);

    /** Applies one server-validated two-tile jump command. */
    GameSession jump(UUID gameId, UUID ownerUserId, Direction direction);

    /** Pauses a running session and freezes its deadline. */
    GameSession pause(UUID gameId, UUID ownerUserId);

    /** Resumes a paused session and restores its deadline relative to the pause duration. */
    GameSession resume(UUID gameId, UUID ownerUserId);

    /** Returns an existing session only when it belongs to the requesting user. */
    GameSession getGame(UUID gameId, UUID ownerUserId);
}
