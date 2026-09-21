package com.makstuk.escapethemaze.backend.configuration;

import com.makstuk.escapethemaze.backend.security.websocket.WebSocketAuthenticationHandshakeInterceptor;
import com.makstuk.escapethemaze.backend.websocket.GameWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/** Registers native WebSocket transport endpoints. */
@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final GameWebSocketHandler gameWebSocketHandler;
    private final WebSocketAuthenticationHandshakeInterceptor authenticationHandshakeInterceptor;

    public WebSocketConfiguration(
            GameWebSocketHandler gameWebSocketHandler,
            WebSocketAuthenticationHandshakeInterceptor authenticationHandshakeInterceptor) {
        this.gameWebSocketHandler = gameWebSocketHandler;
        this.authenticationHandshakeInterceptor = authenticationHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameWebSocketHandler, "/ws/games/{gameId}")
                .addInterceptors(authenticationHandshakeInterceptor)
                .setAllowedOriginPatterns(
                    "http://localhost:5173",
                    "https://escape-the-maze-game-production.up.railway.app"
                );
    }
}
