package management;

import algorithms.GeneratorType;
import entities.MazeObjects.Maze;

public final class MazePreview {

    private static final int PREVIEW_HEIGHT = 10;
    private static final int PREVIEW_WIDTH = 10;

    private MazePreview() {
    }

    public static void main(String[] args) {
        for (GeneratorType generatorType : GeneratorType.values()) {
            printMaze(generatorType);
        }
    }

    private static void printMaze(GeneratorType generatorType) {
        Maze maze = new Maze(PREVIEW_HEIGHT, PREVIEW_WIDTH, generatorType);
        System.out.println();
        System.out.println(generatorType);

        for (int row = 0; row < maze.getHeight(); row++) {
            StringBuilder line = new StringBuilder(maze.getWidth());
            for (int column = 0; column < maze.getWidth(); column++) {
                line.append(maze.getValue(row, column) == 0 ? "⬜" : "⬛");
            }
            System.out.println(line);
        }
    }
}
