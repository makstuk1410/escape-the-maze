package com.makstuk.escapethemaze.backend.api.level;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.makstuk.escapethemaze.backend.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LevelController.class)
@AutoConfigureMockMvc(addFilters = false)
class LevelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void returnsEveryFixedLevelConfiguration() throws Exception {
        mockMvc.perform(get("/api/levels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].difficulty").value("EASY"))
                .andExpect(jsonPath("$[0].generator").value("DFS"))
                .andExpect(jsonPath("$[0].logicalMazeWidth").value(15))
                .andExpect(jsonPath("$[0].gridWidth").value(31))
                .andExpect(jsonPath("$[1].difficulty").value("NORMAL"))
                .andExpect(jsonPath("$[1].generator").value("PRIM"))
                .andExpect(jsonPath("$[2].difficulty").value("HARD"))
                .andExpect(jsonPath("$[2].generator").value("KRUSKAL"))
                .andExpect(jsonPath("$[3].difficulty").value("EXPERT"))
                .andExpect(jsonPath("$[3].gridHeight").value(61))
                .andExpect(jsonPath("$[3].timeLimitSeconds").value(300));
    }
}
