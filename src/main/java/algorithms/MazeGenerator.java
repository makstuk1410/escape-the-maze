package algorithms;

import entities.MazeObjects.MazeGrid;
import java.util.Random;

public abstract class MazeGenerator {

    protected MazeGrid maze;
    protected final int[][] directions = {
        {0, -2},
        {2, 0},
        {0, 2},
        {-2, 0}
    };
    protected final Random random = new Random();
    
    MazeGenerator(MazeGrid maze) {
        this.maze = maze;
    }

    protected void fillMaze() {
        for (int i = 0; i < maze.getHeight(); i++) {
            for (int j = 0; j < maze.getWidth(); j++) {
                maze.setValue(i, j, 1);
            }
        }
    }

    protected boolean inBounds(int y, int x) {
        return x > 0 && x < maze.getWidth() - 1 && y > 0 && y < maze.getHeight() - 1;
    }

    public abstract void createRandomMaze();
}
