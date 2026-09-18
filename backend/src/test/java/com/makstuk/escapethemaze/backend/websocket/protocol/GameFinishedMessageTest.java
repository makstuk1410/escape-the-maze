package com.makstuk.escapethemaze.backend.websocket.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.makstuk.escapethemaze.backend.domain.game.GameStatus;
import org.junit.jupiter.api.Test;

class GameFinishedMessageTest {

    @Test
    void acceptsEveryTerminalStatus() {
        assertThat(new GameFinishedMessage("GAME_FINISHED", 5, GameStatus.WON, 110).status()).isEqualTo(GameStatus.WON);
        assertThat(new GameFinishedMessage("GAME_FINISHED", 5, GameStatus.LOST, 20).status()).isEqualTo(GameStatus.LOST);
        assertThat(new GameFinishedMessage("GAME_FINISHED", 5, GameStatus.TIMED_OUT, 40).status())
                .isEqualTo(GameStatus.TIMED_OUT);
    }

    @Test
    void rejectsNonTerminalStatuses() {
        assertThatThrownBy(() -> new GameFinishedMessage("GAME_FINISHED", 5, GameStatus.RUNNING, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("terminal");
    }
}
