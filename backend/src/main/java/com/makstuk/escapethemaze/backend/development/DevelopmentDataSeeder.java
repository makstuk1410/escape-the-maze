package com.makstuk.escapethemaze.backend.development;

import com.makstuk.escapethemaze.backend.persistence.user.User;
import com.makstuk.escapethemaze.backend.persistence.user.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Local-development data only. This class is never active under the production profile.
 */
@Component
@Profile("dev")
public class DevelopmentDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DevelopmentDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createUserIfMissing("maze_tester", "tester@escape-the-maze.local", "TestMaze123!");
        createUserIfMissing("leaderboard_hero", "hero@escape-the-maze.local", "HeroMaze123!");
    }

    private void createUserIfMissing(String username, String email, String password) {
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            return;
        }

        userRepository.save(new User(
                UUID.randomUUID(),
                username,
                email,
                passwordEncoder.encode(password),
                Instant.now()));
    }
}
