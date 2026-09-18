package com.makstuk.escapethemaze.backend.websocket;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makstuk.escapethemaze.backend.application.game.GameApplicationService;
import com.makstuk.escapethemaze.backend.domain.game.Direction;
import com.makstuk.escapethemaze.backend.security.jwt.AuthenticatedUser;
import com.makstuk.escapethemaze.backend.security.websocket.WebSocketAuthenticationHandshakeInterceptor;
import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.Test;
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
}
