package com.makstuk.escapethemaze.backend.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makstuk.escapethemaze.backend.application.game.GameApplicationService;
import com.makstuk.escapethemaze.backend.security.jwt.AuthenticatedUser;
import com.makstuk.escapethemaze.backend.security.websocket.WebSocketAuthenticationHandshakeInterceptor;
import com.makstuk.escapethemaze.backend.websocket.protocol.MoveCommand;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Transport adapter for one game's native WebSocket connection.
 */
@Component
public class GameWebSocketHandler extends TextWebSocketHandler implements GameCommandGateway {

    private final ObjectMapper objectMapper;
    private final GameApplicationService gameApplicationService;

    public GameWebSocketHandler(ObjectMapper objectMapper, GameApplicationService gameApplicationService) {
        this.objectMapper = objectMapper;
        this.gameApplicationService = gameApplicationService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        MoveCommand command = deserializeMoveCommand(message.getPayload());
        if (command == null || !isValid(command)) {
            return;
        }

        AuthenticatedUser user = (AuthenticatedUser) session.getAttributes()
                .get(WebSocketAuthenticationHandshakeInterceptor.AUTHENTICATED_USER_ATTRIBUTE);
        UUID gameId = (UUID) session.getAttributes()
                .get(WebSocketAuthenticationHandshakeInterceptor.GAME_ID_ATTRIBUTE);
        if (user == null || gameId == null) {
            return;
        }

        gameApplicationService.move(gameId, user.id(), command.direction());
    }

    private MoveCommand deserializeMoveCommand(String payload) {
        try {
            return objectMapper.readValue(payload, MoveCommand.class);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    private boolean isValid(MoveCommand command) {
        return "MOVE".equals(command.type()) && command.commandId() != null && command.direction() != null;
    }
}
