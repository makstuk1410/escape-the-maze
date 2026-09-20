package com.makstuk.escapethemaze.backend.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makstuk.escapethemaze.backend.application.game.GameApplicationService;
import com.makstuk.escapethemaze.backend.domain.game.GameSession;
import com.makstuk.escapethemaze.backend.domain.game.GameStatus;
import com.makstuk.escapethemaze.backend.domain.maze.TileType;
import com.makstuk.escapethemaze.backend.security.jwt.AuthenticatedUser;
import com.makstuk.escapethemaze.backend.security.websocket.WebSocketAuthenticationHandshakeInterceptor;
import com.makstuk.escapethemaze.backend.websocket.protocol.MoveCommand;
import com.makstuk.escapethemaze.backend.websocket.protocol.GameFinishedMessage;
import com.makstuk.escapethemaze.backend.websocket.protocol.StateMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
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

        TileType[][] tilesBeforeMove = gameApplicationService.getGame(gameId, user.id()).getMaze().getTiles();
        GameSession updatedSession = "JUMP".equals(command.type())
                ? gameApplicationService.jump(gameId, user.id(), command.direction())
                : gameApplicationService.move(gameId, user.id(), command.direction());
        if (updatedSession == null) {
            return;
        }
        sendState(session, updatedSession, changedTiles(tilesBeforeMove, updatedSession.getMaze().getTiles()));
        if (isTerminal(updatedSession.getStatus())) {
            sendGameFinished(session, updatedSession);
        }
    }

    private MoveCommand deserializeMoveCommand(String payload) {
        try {
            return objectMapper.readValue(payload, MoveCommand.class);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    private boolean isValid(MoveCommand command) {
        return ("MOVE".equals(command.type()) || "JUMP".equals(command.type()))
                && command.commandId() != null
                && command.direction() != null;
    }

    private boolean isTerminal(GameStatus status) {
        return status == GameStatus.WON || status == GameStatus.LOST || status == GameStatus.TIMED_OUT;
    }

    private void sendGameFinished(WebSocketSession session, GameSession gameSession) throws IOException {
        String payload = objectMapper.writeValueAsString(GameFinishedMessage.from(gameSession));
        session.sendMessage(new TextMessage(payload));
    }

    private void sendState(
            WebSocketSession session,
            GameSession gameSession,
            List<StateMessage.ChangedTile> changedTiles) throws IOException {
        String payload = objectMapper.writeValueAsString(StateMessage.from(gameSession, changedTiles));
        session.sendMessage(new TextMessage(payload));
    }

    private List<StateMessage.ChangedTile> changedTiles(TileType[][] before, TileType[][] after) {
        List<StateMessage.ChangedTile> changedTiles = new ArrayList<>();
        for (int y = 0; y < after.length; y++) {
            for (int x = 0; x < after[y].length; x++) {
                if (before[y][x] != after[y][x]) {
                    changedTiles.add(new StateMessage.ChangedTile(x, y, after[y][x]));
                }
            }
        }
        return List.copyOf(changedTiles);
    }
}
