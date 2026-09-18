package com.makstuk.escapethemaze.backend.websocket.protocol;

import com.makstuk.escapethemaze.backend.domain.game.Direction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

/** One client request to move a player by one logical maze tile. */
public record MoveCommand(
        @NotBlank @Pattern(regexp = "MOVE", message = "type must be MOVE") String type,
        @NotNull UUID commandId,
        @NotNull Direction direction) {
}
