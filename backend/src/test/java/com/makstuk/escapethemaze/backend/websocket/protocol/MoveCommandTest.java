package com.makstuk.escapethemaze.backend.websocket.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makstuk.escapethemaze.backend.domain.game.Direction;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MoveCommandTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesTheDocumentedMoveMessage() throws Exception {
        UUID commandId = UUID.fromString("8e7d6cf1-4e8e-4b9d-b14c-58fdf10ad67f");

        MoveCommand command = objectMapper.readValue("""
                {
                  "type": "MOVE",
                  "commandId": "8e7d6cf1-4e8e-4b9d-b14c-58fdf10ad67f",
                  "direction": "RIGHT"
                }
                """, MoveCommand.class);

        assertThat(command.type()).isEqualTo("MOVE");
        assertThat(command.commandId()).isEqualTo(commandId);
        assertThat(command.direction()).isEqualTo(Direction.RIGHT);
    }

    @Test
    void rejectsAnUnknownDirectionDuringDeserialization() {
        assertThatThrownBy(() -> objectMapper.readValue("""
                {"type":"MOVE","commandId":"8e7d6cf1-4e8e-4b9d-b14c-58fdf10ad67f","direction":"DIAGONAL"}
                """, MoveCommand.class))
                .isInstanceOf(Exception.class);
    }
}
