package com.makstuk.escapethemaze.backend.websocket.protocol;

import com.makstuk.escapethemaze.backend.domain.game.Direction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

/** One client request to move one tile or jump two tiles in a direction. */
public record MoveCommand(
        @NotBlank @Pattern(regexp = "MOVE|JUMP", message = "type must be MOVE or JUMP") String type,
        @NotNull UUID commandId,
        @NotNull Direction direction) {
}
