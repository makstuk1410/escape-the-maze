package com.makstuk.escapethemaze.backend.api.leaderboard;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.makstuk.escapethemaze.backend.application.leaderboard.LeaderboardService;
import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import com.makstuk.escapethemaze.backend.domain.game.GameStatus;
import com.makstuk.escapethemaze.backend.security.jwt.JwtAuthenticationFilter;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LeaderboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaderboardService leaderboardService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void returnsOneDifficultyWithRankedPublicEntries() throws Exception {
        Instant endedAt = Instant.parse("2026-09-18T12:00:00Z");
        when(leaderboardService.getLeaderboard(Difficulty.HARD)).thenReturn(new LeaderboardService.Leaderboard(
                Difficulty.HARD,
                List.of(new LeaderboardService.Entry("maze_tester", 120, GameStatus.WON, endedAt))));

        mockMvc.perform(get("/api/leaderboard").param("difficulty", "HARD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.difficulty").value("HARD"))
                .andExpect(jsonPath("$.entries[0].rank").value(1))
                .andExpect(jsonPath("$.entries[0].username").value("maze_tester"))
                .andExpect(jsonPath("$.entries[0].score").value(120))
                .andExpect(jsonPath("$.entries[0].status").value("WON"))
                .andExpect(jsonPath("$.entries[0].endedAt").value(endedAt.toString()));

        verify(leaderboardService).getLeaderboard(Difficulty.HARD);
    }

    @Test
    void rejectsAMissingDifficulty() throws Exception {
        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().isBadRequest());
    }
}
