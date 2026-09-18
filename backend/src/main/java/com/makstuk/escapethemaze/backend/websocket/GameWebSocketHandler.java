package com.makstuk.escapethemaze.backend.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Transport adapter for one game's native WebSocket connection.
 * Command parsing and authorization are added in subsequent WebSocket tasks.
 */
@Component
public class GameWebSocketHandler extends TextWebSocketHandler implements GameCommandGateway {
}
