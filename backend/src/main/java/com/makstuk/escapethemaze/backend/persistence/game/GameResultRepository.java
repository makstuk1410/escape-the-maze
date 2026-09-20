package com.makstuk.escapethemaze.backend.persistence.game;

import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Database access boundary for durable completed-game records. */
public interface GameResultRepository extends JpaRepository<GameResult, UUID> {

    boolean existsByGameId(UUID gameId);

    /**
     * Returns one difficulty's verified ranking: highest score first, then the
     * earlier completed game wins a score tie.
     */
    @Query("""
            select result
            from GameResult result
            join fetch result.user
            where result.difficulty = :difficulty
            order by result.score desc, result.endedAt asc
            """)
    List<GameResult> findLeaderboardByDifficulty(@Param("difficulty") Difficulty difficulty);
}
