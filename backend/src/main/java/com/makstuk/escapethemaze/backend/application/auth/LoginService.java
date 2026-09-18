package com.makstuk.escapethemaze.backend.application.auth;

import com.makstuk.escapethemaze.backend.api.auth.AuthResponse;
import com.makstuk.escapethemaze.backend.api.auth.LoginRequest;
import com.makstuk.escapethemaze.backend.persistence.user.User;
import com.makstuk.escapethemaze.backend.persistence.user.UserRepository;
import com.makstuk.escapethemaze.backend.security.jwt.GeneratedToken;
import com.makstuk.escapethemaze.backend.security.jwt.JwtTokenService;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LoginService {

    private static final String INVALID_CREDENTIALS = "Invalid email/username or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public LoginService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.identifier().trim();
        User user = userRepository
                .findByEmail(identifier.toLowerCase(Locale.ROOT))
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        GeneratedToken token = jwtTokenService.generate(user.getId(), user.getUsername());
        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                token.value(),
                "Bearer",
                token.expiresAt());
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
    }
}
