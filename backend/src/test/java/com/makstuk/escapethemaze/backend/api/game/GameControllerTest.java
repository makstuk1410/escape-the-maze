package com.makstuk.escapethemaze.backend.api.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.makstuk.escapethemaze.backend.application.game.GameApplicationService;
import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import com.makstuk.escapethemaze.backend.domain.game.GameSession;
import com.makstuk.escapethemaze.backend.domain.game.GameStatus;
import com.makstuk.escapethemaze.backend.domain.game.PlayerState;
import com.makstuk.escapethemaze.backend.domain.maze.GeneratorType;
import com.makstuk.escapethemaze.backend.domain.maze.Maze;
import com.makstuk.escapethemaze.backend.domain.maze.Position;
import com.makstuk.escapethemaze.backend.domain.maze.TileType;
import com.makstuk.escapethemaze.backend.security.jwt.AuthenticatedUser;
import com.makstuk.escapethemaze.backend.security.jwt.JwtAuthenticationFilter;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GameController.class)
@AutoConfigureMockMvc(addFilters = false)
class GameControllerTest {

    private static final UUID USER_ID = UUID.fromString("07d89ee0-83a3-40bd-a98f-9e2089a25ae1");
    private static final UUID GAME_ID = UUID.fromString("13c94f76-5298-4019-8ec8-af1bd048ccd9");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameController gameController;

    @MockitoBean
    private GameApplicationService gameApplicationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createsAnOwnedGameAndReturnsItsInitialState() {
        GameSession session = session();
        when(gameApplicationService.createGame(USER_ID, Difficulty.EASY)).thenReturn(session);

        var response = gameController.createGame(
                new CreateGameRequest(Difficulty.EASY), new AuthenticatedUser(USER_ID, "maze_tester"));

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/games/" + GAME_ID);
        assertThat(response.getBody())
                .hasFieldOrPropertyWithValue("gameId", GAME_ID)
                .hasFieldOrPropertyWithValue("difficulty", Difficulty.EASY)
                .hasFieldOrPropertyWithValue("generator", GeneratorType.DFS)
                .hasFieldOrPropertyWithValue("width", 3)
                .hasFieldOrPropertyWithValue("height", 3)
                .hasFieldOrPropertyWithValue("score", 0)
                .hasFieldOrPropertyWithValue("status", GameStatus.RUNNING);
        assertThat(response.getBody().player()).isEqualTo(new GameStateResponse.PlayerResponse(1, 1, 100));
        assertThat(response.getBody().tiles()[1][1]).isEqualTo(TileType.EMPTY);
        assertThat(response.getBody().effects()).isEqualTo(new GameStateResponse.Effects(null, null));

        verify(gameApplicationService).createGame(USER_ID, Difficulty.EASY);
    }

    @Test
    void rejectsARequestWithoutDifficulty() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsTheOwnersExistingGameState() {
        GameSession session = session();
        when(gameApplicationService.getGame(GAME_ID, USER_ID)).thenReturn(session);

        var response = gameController.getGame(GAME_ID, new AuthenticatedUser(USER_ID, "maze_tester"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().gameId()).isEqualTo(GAME_ID);
        assertThat(response.getBody().tiles()[2][1]).isEqualTo(TileType.EXIT);
        verify(gameApplicationService).getGame(GAME_ID, USER_ID);
    }

    @Test
    void pausesAndResumesTheOwnersGame() {
        GameSession session = session();
        when(gameApplicationService.pause(GAME_ID, USER_ID)).thenReturn(session);
        when(gameApplicationService.resume(GAME_ID, USER_ID)).thenReturn(session);

        var paused = gameController.pauseGame(GAME_ID, new AuthenticatedUser(USER_ID, "maze_tester"));
        var resumed = gameController.resumeGame(GAME_ID, new AuthenticatedUser(USER_ID, "maze_tester"));

        assertThat(paused.getStatusCode().value()).isEqualTo(200);
        assertThat(resumed.getStatusCode().value()).isEqualTo(200);
        verify(gameApplicationService).pause(GAME_ID, USER_ID);
        verify(gameApplicationService).resume(GAME_ID, USER_ID);
    }

    private GameSession session() {
        Instant startedAt = Instant.parse("2026-09-18T12:00:00Z");
        TileType[][] tiles = {
            {TileType.WALL, TileType.WALL, TileType.WALL},
            {TileType.WALL, TileType.EMPTY, TileType.WALL},
            {TileType.WALL, TileType.EXIT, TileType.WALL}
        };
        Maze maze = new Maze(tiles, new Position(1, 1), new Position(1, 2), GeneratorType.DFS);
        return new GameSession(
                GAME_ID,
                USER_ID,
                Difficulty.EASY,
                maze,
                new PlayerState(1, 1),
                startedAt,
                startedAt.plusSeconds(300));
    }
}
