package com.makstuk.escapethemaze.backend.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.makstuk.escapethemaze.backend.application.auth.CurrentUserService;
import com.makstuk.escapethemaze.backend.application.auth.LoginService;
import com.makstuk.escapethemaze.backend.application.auth.RegistrationService;
import com.makstuk.escapethemaze.backend.persistence.user.User;
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

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    private static final UUID USER_ID = UUID.fromString("07d89ee0-83a3-40bd-a98f-9e2089a25ae1");
    private static final Instant CREATED_AT = Instant.parse("2026-09-18T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthController authController;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void registersAValidAccount() throws Exception {
        User user = new User(USER_ID, "maze_tester", "tester@example.com", "hash", CREATED_AT);
        when(registrationService.register(any(RegisterRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"maze_tester","email":"tester@example.com","password":"TestMaze123!"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/" + USER_ID))
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.username").value("maze_tester"))
                .andExpect(jsonPath("$.email").value("tester@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(registrationService).register(any(RegisterRequest.class));
    }

    @Test
    void rejectsAnInvalidRegistrationBeforeCallingTheService() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"ab","email":"not-an-email","password":"short"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(registrationService);
    }

    @Test
    void logsInWithValidCredentials() throws Exception {
        Instant expiresAt = Instant.parse("2026-09-18T18:00:00Z");
        AuthResponse response = new AuthResponse(
                USER_ID, "maze_tester", "tester@example.com", "signed.jwt.token", "Bearer", expiresAt);
        when(loginService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"maze_tester","password":"TestMaze123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("signed.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").value(expiresAt.toString()));

        verify(loginService).login(any(LoginRequest.class));
    }

    @Test
    void rejectsALoginRequestWithoutAPasswordBeforeCallingTheService() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"maze_tester"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(loginService);
    }

    @Test
    void returnsTheCurrentAuthenticatedUser() {
        CurrentUserResponse expected = new CurrentUserResponse(
                USER_ID, "maze_tester", "tester@example.com", CREATED_AT);
        when(currentUserService.getCurrentUser(USER_ID)).thenReturn(expected);

        var response = authController.me(new AuthenticatedUser(USER_ID, "maze_tester"));

        assertThat(response.getBody()).isEqualTo(expected);
        verify(currentUserService).getCurrentUser(USER_ID);
    }

    @Test
    void clearsTheAccessTokenCookieOnLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("access_token=")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));
    }
}
