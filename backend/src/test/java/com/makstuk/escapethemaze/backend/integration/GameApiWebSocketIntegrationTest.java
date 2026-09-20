package com.makstuk.escapethemaze.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makstuk.escapethemaze.backend.application.game.GameApplicationService;
import com.makstuk.escapethemaze.backend.domain.maze.TileType;
import com.makstuk.escapethemaze.backend.persistence.game.GameResultRepository;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.url=jdbc:h2:mem:escape_the_maze_integration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.flyway.enabled=false",
            "app.jwt.secret=dGhpcy1pcy1hLWRldmVsb3BtZW50LW9ubHktc2VjcmV0LWNoYW5nZS1tZQ=="
        })
class GameApiWebSocketIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameApplicationService gameApplicationService;

    @Autowired
    private GameResultRepository gameResultRepository;

    @LocalServerPort
    private int port;

    @Test
    void createsAnAuthenticatedGameAndFinishesItThroughWebSocket() throws Exception {
        String username = "integration_player";
        String email = "integration@example.com";
        UUID userId = register(username, email);
        String accessToken = login(username);
        UUID gameId = createGame(accessToken);

        var session = gameApplicationService.getGame(gameId, userId);
        session.getMaze().replaceTile(session.getPlayer().x(), session.getPlayer().y() + 1, TileType.EXIT);

        MessageCollector handler = new MessageCollector();
        WebSocketSession socket = new StandardWebSocketClient()
                .execute(
                        handler,
                        "ws://localhost:" + port + "/ws/games/" + gameId + "?access_token=" + accessToken)
                .get(5, TimeUnit.SECONDS);
        try {
            socket.sendMessage(new TextMessage("""
                    {"type":"MOVE","commandId":"8e7d6cf1-4e8e-4b9d-b14c-58fdf10ad67f","direction":"DOWN"}
                    """));

            JsonNode state = objectMapper.readTree(handler.messages.poll(5, TimeUnit.SECONDS));
            assertThat(state.get("type").asText()).isEqualTo("STATE");
            JsonNode event = objectMapper.readTree(handler.messages.poll(5, TimeUnit.SECONDS));
            assertThat(event.get("type").asText()).isEqualTo("GAME_FINISHED");
            assertThat(event.get("status").asText()).isEqualTo("WON");
            assertThat(gameResultRepository.existsByGameId(gameId)).isTrue();

            ResponseEntity<JsonNode> leaderboard = restTemplate.getForEntity(
                    "/api/leaderboard?difficulty=EASY", JsonNode.class);
            assertThat(leaderboard.getStatusCode().value()).isEqualTo(200);
            assertThat(leaderboard.getBody().get("entries").get(0).get("username").asText()).isEqualTo(username);
        } finally {
            socket.close();
        }
    }

    private UUID register(String username, String email) {
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/api/auth/register",
                jsonEntity(Map.of("username", username, "email", email, "password", "TestMaze123!")),
                JsonNode.class);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        return UUID.fromString(response.getBody().get("id").asText());
    }

    private String login(String username) {
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/api/auth/login",
                jsonEntity(Map.of("identifier", username, "password", "TestMaze123!")),
                JsonNode.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        return response.getBody().get("accessToken").asText();
    }

    private UUID createGame(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/api/games",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("difficulty", "EASY"), headers),
                JsonNode.class);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        return UUID.fromString(response.getBody().get("gameId").asText());
    }

    private HttpEntity<Map<String, String>> jsonEntity(Map<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private static class MessageCollector extends TextWebSocketHandler {

        private final java.util.concurrent.BlockingQueue<String> messages = new java.util.concurrent.LinkedBlockingQueue<>();

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            messages.add(message.getPayload());
        }
    }
}
