package com.makstuk.escapethemaze.backend.api.game;

import com.makstuk.escapethemaze.backend.application.game.GameApplicationService;
import com.makstuk.escapethemaze.backend.domain.game.GameSession;
import com.makstuk.escapethemaze.backend.security.jwt.AuthenticatedUser;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST entry point for creating and later recovering game sessions. */
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameApplicationService gameApplicationService;

    public GameController(GameApplicationService gameApplicationService) {
        this.gameApplicationService = gameApplicationService;
    }

    @PostMapping
    public ResponseEntity<GameStateResponse> createGame(
            @Valid @RequestBody CreateGameRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        GameSession session = gameApplicationService.createGame(authenticatedUser.id(), request.difficulty());
        return ResponseEntity
                .created(URI.create("/api/games/" + session.getId()))
                .body(GameStateResponse.from(session));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> getGame(
            @PathVariable UUID gameId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        GameSession session = gameApplicationService.getGame(gameId, authenticatedUser.id());
        return ResponseEntity.ok(GameStateResponse.from(session));
    }

    @PostMapping("/{gameId}/pause")
    public ResponseEntity<GameStateResponse> pauseGame(
            @PathVariable UUID gameId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        GameSession session = gameApplicationService.pause(gameId, authenticatedUser.id());
        return ResponseEntity.ok(GameStateResponse.from(session));
    }

    @PostMapping("/{gameId}/resume")
    public ResponseEntity<GameStateResponse> resumeGame(
            @PathVariable UUID gameId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        GameSession session = gameApplicationService.resume(gameId, authenticatedUser.id());
        return ResponseEntity.ok(GameStateResponse.from(session));
    }
}
