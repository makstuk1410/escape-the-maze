package algorithms;

import entities.Maze;

public abstract class MazeGenerator {

    protected Maze maze;
    protected final int[][] directions = {
        {0, -2}, // вверх
        {2, 0}, // вправо
        {0, 2}, // вниз
        {-2, 0} // вліво
    };
    
    
    MazeGenerator(Maze maze) {
        this.maze = maze;
    }



    protected boolean inBounds(int x, int y) {
        return x > 0 && x < maze.getWidth() - 1 && y > 0 && y < maze.getHeight() - 1;
    }

    public abstract void createRandomMaze();
}
