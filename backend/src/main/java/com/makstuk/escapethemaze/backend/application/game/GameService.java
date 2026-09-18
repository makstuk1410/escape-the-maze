package com.makstuk.escapethemaze.backend.application.game;

import com.makstuk.escapethemaze.backend.domain.game.Difficulty;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

/**
 * Creates and retains authoritative in-memory game sessions.
 */
@Service
public class GameService implements GameApplicationService {

    private final Clock clock;
    private final Random random;
    private final ConcurrentMap<UUID, GameSession> sessions = new ConcurrentHashMap<>();

    public GameService(Clock clock) {
        this(clock, new Random());
    }

    GameService(Clock clock, Random random) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.random = Objects.requireNonNull(random, "random must not be null");
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

    @Override
    public Optional<GameSession> findGame(UUID gameId) {
        return Optional.ofNullable(sessions.get(gameId));
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

        return new Maze(tiles, new Position(grid.getStartX(), grid.getStartY()), exit, generatorType);
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
