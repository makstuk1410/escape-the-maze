package com.makstuk.escapethemaze.backend.api.auth;

import com.makstuk.escapethemaze.backend.application.auth.RegistrationService;
import com.makstuk.escapethemaze.backend.application.auth.LoginService;
import com.makstuk.escapethemaze.backend.application.auth.CurrentUserService;
import com.makstuk.escapethemaze.backend.persistence.user.User;
import com.makstuk.escapethemaze.backend.security.jwt.AuthenticatedUser;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final LoginService loginService;
    private final CurrentUserService currentUserService;

    public AuthController(
            RegistrationService registrationService,
            LoginService loginService,
            CurrentUserService currentUserService) {
        this.registrationService = registrationService;
        this.loginService = loginService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = registrationService.register(request);
        RegisterResponse response = new RegisterResponse(
                user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());

        return ResponseEntity
                .created(URI.create("/api/users/" + user.getId()))
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(currentUserService.getCurrentUser(authenticatedUser.id()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie expiredAccessToken = ResponseCookie.from("access_token", "")
                .path("/")
                .httpOnly(true)
                .sameSite("Strict")
                .maxAge(Duration.ZERO)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expiredAccessToken.toString())
                .build();
    }
}
