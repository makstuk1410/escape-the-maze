package com.makstuk.escapethemaze.backend.configuration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.makstuk.escapethemaze.backend.websocket.GameWebSocketHandler;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

class WebSocketConfigurationTest {

    @Test
    void registersTheNativeGameEndpointForTheFrontendOrigin() {
        GameWebSocketHandler handler = new GameWebSocketHandler();
        WebSocketHandlerRegistry registry = org.mockito.Mockito.mock(WebSocketHandlerRegistry.class);
        WebSocketHandlerRegistration registration = org.mockito.Mockito.mock(WebSocketHandlerRegistration.class);
        when(registry.addHandler(handler, "/ws/games/{gameId}")).thenReturn(registration);
        when(registration.setAllowedOriginPatterns("http://localhost:5173")).thenReturn(registration);

        new WebSocketConfiguration(handler).registerWebSocketHandlers(registry);

        verify(registry).addHandler(handler, "/ws/games/{gameId}");
        verify(registration).setAllowedOriginPatterns("http://localhost:5173");
    }
}
