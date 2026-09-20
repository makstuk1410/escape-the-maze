package com.makstuk.escapethemaze.backend.application.auth;

import com.makstuk.escapethemaze.backend.api.auth.RegisterRequest;
import com.makstuk.escapethemaze.backend.persistence.user.User;
import com.makstuk.escapethemaze.backend.persistence.user.UserRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }

        User user = new User(
                UUID.randomUUID(),
                username,
                email,
                passwordEncoder.encode(request.password()),
                Instant.now());

        return userRepository.save(user);
    }
}
