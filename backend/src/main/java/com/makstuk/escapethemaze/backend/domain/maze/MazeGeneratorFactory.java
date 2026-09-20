package com.makstuk.escapethemaze.backend.domain.maze;

import java.util.Objects;
import java.util.Random;

/**
 * Creates the domain generator selected for a new maze.
 */
public final class MazeGeneratorFactory {

    private MazeGeneratorFactory() {
    }

    public static MazeGenerator create(GeneratorType generatorType, MazeGrid maze) {
        return create(generatorType, maze, new Random());
    }

    public static MazeGenerator create(GeneratorType generatorType, MazeGrid maze, Random random) {
        Objects.requireNonNull(generatorType, "generatorType must not be null");
        Objects.requireNonNull(maze, "maze must not be null");
        Objects.requireNonNull(random, "random must not be null");
        return switch (generatorType) {
            case DFS -> new DFSMazeGenerator(maze, random);
            case PRIM -> new PrimsMazeGenerator(maze, random);
            case KRUSKAL -> new KruskalMazeGenerator(maze, random);
            case BINARY_TREE -> new BinaryTreeMazeGenerator(maze, random);
        };
    }

    /**
     * Generates a reproducible numeric grid from one seed.
     */
    public static MazeGrid generate(GeneratorType generatorType, int height, int width, long seed) {
        Random random = new Random(seed);
        MazeGrid maze = new MazeGrid(height, width, random);
        create(generatorType, maze, random).createRandomMaze();
        return maze;
    }
}
