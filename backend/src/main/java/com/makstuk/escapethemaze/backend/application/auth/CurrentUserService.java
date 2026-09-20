package com.makstuk.escapethemaze.backend.application.auth;

import com.makstuk.escapethemaze.backend.api.auth.CurrentUserResponse;
import com.makstuk.escapethemaze.backend.persistence.user.User;
import com.makstuk.escapethemaze.backend.persistence.user.UserRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return new CurrentUserResponse(
                user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());
    }
}
