package com.makstuk.escapethemaze.backend.security.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.makstuk.escapethemaze.backend.security.jwt.AuthenticatedUser;
import com.makstuk.escapethemaze.backend.security.jwt.JwtTokenService;
import java.net.URI;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

class WebSocketAuthenticationHandshakeInterceptorTest {

    @Test
    void acceptsAValidTokenAndStoresTheVerifiedIdentity() {
        JwtTokenService jwtTokenService = mock(JwtTokenService.class);
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), "maze_tester");
        when(jwtTokenService.parse("valid-token")).thenReturn(Optional.of(user));
        ServerHttpRequest request = requestWithUri("http://localhost/ws/games/game-id?access_token=valid-token");
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        var attributes = new HashMap<String, Object>();

        boolean accepted = new WebSocketAuthenticationHandshakeInterceptor(jwtTokenService)
                .beforeHandshake(request, response, mock(WebSocketHandler.class), attributes);

        assertThat(accepted).isTrue();
        assertThat(attributes).containsEntry(
                WebSocketAuthenticationHandshakeInterceptor.AUTHENTICATED_USER_ATTRIBUTE, user);
    }

    @Test
    void rejectsMissingOrInvalidTokensBeforeOpeningTheSocket() {
        JwtTokenService jwtTokenService = mock(JwtTokenService.class);
        ServerHttpResponse missingTokenResponse = mock(ServerHttpResponse.class);
        ServerHttpResponse invalidTokenResponse = mock(ServerHttpResponse.class);
        when(jwtTokenService.parse("invalid-token")).thenReturn(Optional.empty());
        WebSocketAuthenticationHandshakeInterceptor interceptor =
                new WebSocketAuthenticationHandshakeInterceptor(jwtTokenService);

        assertThat(interceptor.beforeHandshake(
                        requestWithUri("http://localhost/ws/games/game-id"),
                        missingTokenResponse,
                        mock(WebSocketHandler.class),
                        new HashMap<>()))
                .isFalse();
        assertThat(interceptor.beforeHandshake(
                        requestWithUri("http://localhost/ws/games/game-id?access_token=invalid-token"),
                        invalidTokenResponse,
                        mock(WebSocketHandler.class),
                        new HashMap<>()))
                .isFalse();

        verify(missingTokenResponse).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(invalidTokenResponse).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    private ServerHttpRequest requestWithUri(String value) {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getURI()).thenReturn(URI.create(value));
        return request;
    }
}
