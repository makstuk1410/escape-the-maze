package com.makstuk.escapethemaze.backend.persistence.game;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access boundary for durable completed-game records. */
public interface GameResultRepository extends JpaRepository<GameResult, UUID> {

    boolean existsByGameId(UUID gameId);
}
