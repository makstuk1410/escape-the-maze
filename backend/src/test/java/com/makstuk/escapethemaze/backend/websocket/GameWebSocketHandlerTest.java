package com.makstuk.escapethemaze.backend.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makstuk.escapethemaze.backend.application.game.GameApplicationService;
import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import com.makstuk.escapethemaze.backend.domain.game.Direction;
import com.makstuk.escapethemaze.backend.domain.game.GameSession;
import com.makstuk.escapethemaze.backend.domain.game.GameStatus;
import com.makstuk.escapethemaze.backend.domain.game.PlayerState;
import com.makstuk.escapethemaze.backend.domain.maze.GeneratorType;
import com.makstuk.escapethemaze.backend.domain.maze.Maze;
import com.makstuk.escapethemaze.backend.domain.maze.Position;
import com.makstuk.escapethemaze.backend.domain.maze.TileType;
import com.makstuk.escapethemaze.backend.security.jwt.AuthenticatedUser;
import com.makstuk.escapethemaze.backend.security.websocket.WebSocketAuthenticationHandshakeInterceptor;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

class GameWebSocketHandlerTest {

    @Test
    void routesAValidMoveToTheAuthenticatedOwnersGame() throws Exception {
        GameApplicationService gameApplicationService = mock(GameApplicationService.class);
        GameWebSocketHandler handler = new GameWebSocketHandler(new ObjectMapper(), gameApplicationService);
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        WebSocketSession socketSession = authenticatedSession(userId, gameId);

        handler.handleTextMessage(socketSession, new TextMessage("""
                {"type":"MOVE","commandId":"8e7d6cf1-4e8e-4b9d-b14c-58fdf10ad67f","direction":"LEFT"}
                """));

        verify(gameApplicationService).move(gameId, userId, Direction.LEFT);
    }

    @Test
    void doesNotRouteMalformedOrUnsupportedMessages() throws Exception {
        GameApplicationService gameApplicationService = mock(GameApplicationService.class);
        GameWebSocketHandler handler = new GameWebSocketHandler(new ObjectMapper(), gameApplicationService);
        WebSocketSession socketSession = authenticatedSession(UUID.randomUUID(), UUID.randomUUID());

        handler.handleTextMessage(socketSession, new TextMessage("{\"type\":\"PAUSE\"}"));
        handler.handleTextMessage(socketSession, new TextMessage("not-json"));

        verifyNoInteractions(gameApplicationService);
    }

    @Test
    void sendsGameFinishedWhenAMoveEndsTheGame() throws Exception {
        GameApplicationService gameApplicationService = mock(GameApplicationService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        GameWebSocketHandler handler = new GameWebSocketHandler(objectMapper, gameApplicationService);
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        WebSocketSession socketSession = authenticatedSession(userId, gameId);
        GameSession finishedGame = finishedGame();
        when(gameApplicationService.move(gameId, userId, Direction.RIGHT)).thenReturn(finishedGame);

        handler.handleTextMessage(socketSession, new TextMessage("""
                {"type":"MOVE","commandId":"8e7d6cf1-4e8e-4b9d-b14c-58fdf10ad67f","direction":"RIGHT"}
                """));

        ArgumentCaptor<TextMessage> event = ArgumentCaptor.forClass(TextMessage.class);
        verify(socketSession).sendMessage(event.capture());
        var json = objectMapper.readTree(event.getValue().getPayload());
        assertThat(json.get("type").asText()).isEqualTo("GAME_FINISHED");
        assertThat(json.get("status").asText()).isEqualTo("WON");
        assertThat(json.get("score").asInt()).isZero();
        assertThat(json.get("stateVersion").asLong()).isEqualTo(1);
    }

    private WebSocketSession authenticatedSession(UUID userId, UUID gameId) {
        WebSocketSession session = mock(WebSocketSession.class);
        var attributes = new HashMap<String, Object>();
        attributes.put(
                WebSocketAuthenticationHandshakeInterceptor.AUTHENTICATED_USER_ATTRIBUTE,
                new AuthenticatedUser(userId, "maze_tester"));
        attributes.put(WebSocketAuthenticationHandshakeInterceptor.GAME_ID_ATTRIBUTE, gameId);
        when(session.getAttributes()).thenReturn(attributes);
        return session;
    }

    private GameSession finishedGame() {
        Instant startedAt = Instant.parse("2026-09-18T12:00:00Z");
        TileType[][] tiles = {
            {TileType.WALL, TileType.WALL, TileType.WALL},
            {TileType.WALL, TileType.EMPTY, TileType.WALL},
            {TileType.WALL, TileType.EXIT, TileType.WALL}
        };
        Maze maze = new Maze(tiles, new Position(1, 1), new Position(1, 2), GeneratorType.DFS);
        GameSession session = new GameSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Difficulty.EASY,
                maze,
                new PlayerState(1, 1),
                startedAt,
                startedAt.plusSeconds(300));
        session.updateStatus(GameStatus.WON);
        return session;
    }
}
