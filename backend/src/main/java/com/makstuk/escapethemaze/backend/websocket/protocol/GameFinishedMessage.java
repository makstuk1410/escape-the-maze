package com.makstuk.escapethemaze.backend.websocket.protocol;

import com.makstuk.escapethemaze.backend.domain.game.GameSession;
import com.makstuk.escapethemaze.backend.domain.game.GameStatus;
import java.util.Objects;

/** Terminal game result sent to the owner after a command ends the game. */
public record GameFinishedMessage(String type, long stateVersion, GameStatus status, int score) {

    public GameFinishedMessage {
        if (!"GAME_FINISHED".equals(type)) {
            throw new IllegalArgumentException("type must be GAME_FINISHED");
        }
        if (!isTerminal(status)) {
            throw new IllegalArgumentException("status must be terminal");
        }
    }

    public static GameFinishedMessage from(GameSession session) {
        Objects.requireNonNull(session, "session must not be null");
        return new GameFinishedMessage(
                "GAME_FINISHED", session.getStateVersion(), session.getStatus(), session.getScore());
    }

    private static boolean isTerminal(GameStatus status) {
        return status == GameStatus.WON || status == GameStatus.LOST || status == GameStatus.TIMED_OUT;
    }
}
