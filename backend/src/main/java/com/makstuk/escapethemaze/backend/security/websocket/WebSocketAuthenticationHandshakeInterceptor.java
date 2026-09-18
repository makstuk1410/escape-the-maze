package com.makstuk.escapethemaze.backend.security.websocket;

import com.makstuk.escapethemaze.backend.application.game.GameApplicationService;
import com.makstuk.escapethemaze.backend.security.jwt.AuthenticatedUser;
import com.makstuk.escapethemaze.backend.security.jwt.JwtTokenService;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Verifies a browser-provided JWT before allowing a native WebSocket handshake.
 */
@Component
public class WebSocketAuthenticationHandshakeInterceptor implements HandshakeInterceptor {

    public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";

    private static final UriTemplate GAME_ENDPOINT = new UriTemplate("/ws/games/{gameId}");

    private final JwtTokenService jwtTokenService;
    private final GameApplicationService gameApplicationService;

    public WebSocketAuthenticationHandshakeInterceptor(
            JwtTokenService jwtTokenService, GameApplicationService gameApplicationService) {
        this.jwtTokenService = jwtTokenService;
        this.gameApplicationService = gameApplicationService;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler webSocketHandler,
            Map<String, Object> attributes) {
        String token = accessTokenFrom(request.getURI());
        if (token == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        return jwtTokenService.parse(token)
                .map(user -> verifyGameOwnership(request.getURI(), user, response, attributes))
                .orElseGet(() -> {
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return false;
                });
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler webSocketHandler,
            Exception exception) {
        // No cleanup is required; attributes are copied to the WebSocket session.
    }

    private String accessTokenFrom(URI uri) {
        MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
        String token = parameters.getFirst("access_token");
        return token == null || token.isBlank() ? null : token;
    }

    private boolean storeAuthenticatedUser(AuthenticatedUser user, Map<String, Object> attributes) {
        attributes.put(AUTHENTICATED_USER_ATTRIBUTE, user);
        return true;
    }

    private boolean verifyGameOwnership(
            URI uri, AuthenticatedUser user, ServerHttpResponse response, Map<String, Object> attributes) {
        try {
            String gameId = GAME_ENDPOINT.match(uri.getPath()).get("gameId");
            gameApplicationService.getGame(UUID.fromString(gameId), user.id());
            return storeAuthenticatedUser(user, attributes);
        } catch (IllegalArgumentException exception) {
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return false;
        } catch (ResponseStatusException exception) {
            response.setStatusCode(exception.getStatusCode());
            return false;
        }
    }
}
