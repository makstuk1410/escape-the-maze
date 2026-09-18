package com.makstuk.escapethemaze.backend.api.auth;

import com.makstuk.escapethemaze.backend.application.auth.RegistrationService;
import com.makstuk.escapethemaze.backend.persistence.user.User;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
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
}
