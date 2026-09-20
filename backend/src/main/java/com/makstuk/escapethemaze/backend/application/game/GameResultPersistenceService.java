package com.makstuk.escapethemaze.backend.application.game;

import com.makstuk.escapethemaze.backend.domain.game.GameSession;
import com.makstuk.escapethemaze.backend.domain.game.GameStatus;
import com.makstuk.escapethemaze.backend.persistence.game.GameResult;
import com.makstuk.escapethemaze.backend.persistence.game.GameResultRepository;
import com.makstuk.escapethemaze.backend.persistence.user.User;
import com.makstuk.escapethemaze.backend.persistence.user.UserRepository;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Persists one server-calculated leaderboard result when a player wins a run. */
@Service
public class GameResultPersistenceService {

    private final GameResultRepository gameResultRepository;
    private final UserRepository userRepository;

    public GameResultPersistenceService(GameResultRepository gameResultRepository, UserRepository userRepository) {
        this.gameResultRepository = gameResultRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void persistIfCompleted(GameSession session, Instant endedAt) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(endedAt, "endedAt must not be null");
        if (session.getStatus() != GameStatus.WON || gameResultRepository.existsByGameId(session.getId())) {
            return;
        }

        User user = userRepository.findById(session.getOwnerUserId())
                .orElseThrow(() -> new IllegalStateException("Game owner was not found"));
        GameResult result = new GameResult(
                UUID.randomUUID(),
                session.getId(),
                user,
                session.getDifficulty(),
                session.getMaze().getGeneratorType(),
                session.getScore(),
                session.getStatus(),
                session.getStartedAt(),
                endedAt);
        gameResultRepository.saveAndFlush(result);
    }

}
