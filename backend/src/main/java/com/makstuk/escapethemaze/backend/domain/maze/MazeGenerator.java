package com.makstuk.escapethemaze.backend.domain.maze;

import java.util.Random;

/**
 * UI-independent base class for maze-generation algorithms.
 */
public abstract class MazeGenerator {

    protected final MazeGrid maze;
    protected final int[][] directions = {
        {0, -2},
        {2, 0},
        {0, 2},
        {-2, 0}
    };
    protected final Random random = new Random();

    protected MazeGenerator(MazeGrid maze) {
        this.maze = maze;
    }

    protected void fillMaze() {
        for (int y = 0; y < maze.getHeight(); y++) {
            for (int x = 0; x < maze.getWidth(); x++) {
                maze.setValue(y, x, 1);
            }
        }
    }

    protected boolean inBounds(int y, int x) {
        return x > 0 && x < maze.getWidth() - 1 && y > 0 && y < maze.getHeight() - 1;
    }

    public abstract void createRandomMaze();
}
