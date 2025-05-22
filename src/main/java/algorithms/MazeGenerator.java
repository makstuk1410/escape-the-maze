package algorithms;

import entities.MazeObjects.Maze;

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

    /*
    public void setMaze(Maze maze){
        this.maze = maze;
    }
    */
    protected void fillMaze() {
        for (int i = 0; i < maze.getHeight(); i++) {
            for (int j = 0; j < maze.getWidth(); j++) {
                maze.setValue(i, j, 1); // Стіна
            }
        }
    }

    protected boolean inBounds(int y, int x) {
        return x > 0 && x < maze.getWidth() - 1 && y > 0 && y < maze.getHeight() - 1;
    }

    public abstract void createRandomMaze();
}
