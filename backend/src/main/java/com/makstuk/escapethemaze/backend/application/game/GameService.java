package com.makstuk.escapethemaze.backend.application.game;

import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
import com.makstuk.escapethemaze.backend.domain.game.Direction;
import com.makstuk.escapethemaze.backend.domain.game.GameRules;
import com.makstuk.escapethemaze.backend.domain.game.GameSession;
import com.makstuk.escapethemaze.backend.domain.game.PlayerState;
import com.makstuk.escapethemaze.backend.domain.maze.GeneratorType;
import com.makstuk.escapethemaze.backend.domain.maze.Maze;
import com.makstuk.escapethemaze.backend.domain.maze.MazeGeneratorFactory;
import com.makstuk.escapethemaze.backend.domain.maze.MazeGrid;
import com.makstuk.escapethemaze.backend.domain.maze.Position;
import com.makstuk.escapethemaze.backend.domain.maze.TileType;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Creates and retains authoritative in-memory game sessions.
 */
@Service
public class GameService implements GameApplicationService {

    private final Clock clock;
    private final Random random;
    private final GameResultPersistenceService gameResultPersistenceService;
    private final ConcurrentMap<UUID, GameSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    public GameService(Clock clock, GameResultPersistenceService gameResultPersistenceService) {
        this(clock, new Random(), gameResultPersistenceService);
    }

    GameService(Clock clock, Random random, GameResultPersistenceService gameResultPersistenceService) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.random = Objects.requireNonNull(random, "random must not be null");
        this.gameResultPersistenceService = Objects.requireNonNull(
                gameResultPersistenceService, "gameResultPersistenceService must not be null");
    }

    @Override
    public GameSession createGame(UUID ownerUserId, Difficulty difficulty) {
        Objects.requireNonNull(ownerUserId, "ownerUserId must not be null");
        Objects.requireNonNull(difficulty, "difficulty must not be null");

        Maze maze = generateMaze(difficulty);
        Instant startedAt = clock.instant();
        GameSession session = new GameSession(
                UUID.randomUUID(),
                ownerUserId,
                difficulty,
                maze,
                new PlayerState(maze.getStart().x(), maze.getStart().y()),
                startedAt,
                startedAt.plus(GameRules.GAME_DURATION));

        sessions.put(session.getId(), session);
        return session;
    }

    /**
     * Applies a single command to the server-side session. Invalid movement is deliberately
     * idempotent: the caller receives the unchanged authoritative state.
     */
    @Override
    public GameSession move(UUID gameId, UUID ownerUserId, Direction direction) {
        Objects.requireNonNull(gameId, "gameId must not be null");
        Objects.requireNonNull(ownerUserId, "ownerUserId must not be null");
        Objects.requireNonNull(direction, "direction must not be null");

        GameSession session = getGame(gameId, ownerUserId);

        synchronized (session) {
            Instant now = clock.instant();
            if (session.timeoutIfExpired(now) || !session.acceptsMovement() || session.isFrozenAt(now)) {
                persistCompletedResult(session, now);
                return session;
            }

            Optional<Position> target = session.validMoveTarget(direction);
            if (target.isEmpty()) {
                return session;
            }

            session.updatePlayer(session.getPlayer().moveTo(target.get().x(), target.get().y()));
            applyTileEffect(session, now);
            persistCompletedResult(session, now);
            return session;
        }
    }

    @Override
    public GameSession getGame(UUID gameId, UUID ownerUserId) {
        Objects.requireNonNull(gameId, "gameId must not be null");
        Objects.requireNonNull(ownerUserId, "ownerUserId must not be null");

        GameSession session = sessions.get(gameId);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game session was not found");
        }
        if (!session.getOwnerUserId().equals(ownerUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this game session");
        }
        return session;
    }

    private void applyTileEffect(GameSession session, Instant now) {
        TileType tile = session.getMaze().tileAt(session.getPlayer().x(), session.getPlayer().y());
        switch (tile) {
            case GOLD -> session.collectGoldAtPlayer();
            case SPIKES -> session.applySpikeDamageAtPlayer(now);
            case FREEZE -> session.applyFreezeAtPlayer(now);
            case FOG -> session.applyFogAtPlayer(now);
            case EXIT -> session.winAtExit();
            case WALL, EMPTY -> {
                // No effect after a successful move.
            }
        }
    }

    private void persistCompletedResult(GameSession session, Instant endedAt) {
        gameResultPersistenceService.persistIfCompleted(session, endedAt);
    }

    private Maze generateMaze(Difficulty difficulty) {
        GeneratorType generatorType = GameRules.generatorFor(difficulty);
        MazeGrid grid = new MazeGrid(
                GameRules.mazeHeightFor(difficulty),
                GameRules.mazeWidthFor(difficulty),
                random);
        MazeGeneratorFactory.create(generatorType, grid, random).createRandomMaze();

        grid.setValue(grid.getStartY(), grid.getStartX(), 0);
        Position exit = addExit(grid);
        TileType[][] tiles = grid.toTileTypes();
        tiles[exit.y()][exit.x()] = TileType.EXIT;
        placeSpecialTiles(tiles, new Position(grid.getStartX(), grid.getStartY()), exit, difficulty);

        return new Maze(tiles, new Position(grid.getStartX(), grid.getStartY()), exit, generatorType);
    }

    private void placeSpecialTiles(TileType[][] tiles, Position start, Position exit, Difficulty difficulty) {
        List<Position> candidates = new ArrayList<>();
        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[y].length; x++) {
                if (tiles[y][x] == TileType.EMPTY
                        && (x != start.x() || y != start.y())
                        && (x != exit.x() || y != exit.y())) {
                    candidates.add(new Position(x, y));
                }
            }
        }

        Collections.shuffle(candidates, random);
        GameRules.SpecialTileCounts counts = GameRules.specialTileCountsFor(difficulty);
        int nextCandidate = 0;
        nextCandidate = placeTiles(tiles, candidates, nextCandidate, counts.gold(), TileType.GOLD);
        nextCandidate = placeTiles(tiles, candidates, nextCandidate, counts.spikes(), TileType.SPIKES);
        nextCandidate = placeTiles(tiles, candidates, nextCandidate, counts.freeze(), TileType.FREEZE);
        placeTiles(tiles, candidates, nextCandidate, counts.fog(), TileType.FOG);
    }

    private int placeTiles(
            TileType[][] tiles,
            List<Position> candidates,
            int startIndex,
            int count,
            TileType tileType) {
        if (startIndex + count > candidates.size()) {
            throw new IllegalStateException("Maze does not have enough walkable cells for special tiles");
        }
        for (int index = startIndex; index < startIndex + count; index++) {
            Position position = candidates.get(index);
            tiles[position.y()][position.x()] = tileType;
        }
        return startIndex + count;
    }

    private Position addExit(MazeGrid grid) {
        int exitRow = grid.getHeight() - 1;
        int penultimateRow = exitRow - 1;
        List<Integer> exitColumns = new ArrayList<>();

        for (int x = 1; x < grid.getWidth() - 1; x += 2) {
            if (grid.getValue(penultimateRow, x) == 0) {
                exitColumns.add(x);
            }
        }
        if (exitColumns.isEmpty()) {
            throw new IllegalStateException("Maze generation produced no exit");
        }

        int exitX = exitColumns.get(random.nextInt(exitColumns.size()));
        grid.setValue(exitRow, exitX, 0);
        return new Position(exitX, exitRow);
    }
}
