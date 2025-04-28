package algorithms;

import entities.Maze;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BinaryTreeMazeGenerator extends MazeGenerator {

    public BinaryTreeMazeGenerator(Maze maze) {
        super(maze);
    }

    @Override
    public void createRandomMaze() {
        fillMaze();
        Random r = new Random();

        for (int y = 1; y < maze.getHeight() - 1; y += 2) {
            for (int x = 1; x < maze.getWidth() - 1; x += 2) {
                maze.setValue(y, x, 0);

                List<int[]> possibleDirections = new ArrayList<>();
/*
                if (y > 1) {
                    possibleDirections.add(new int[]{-1, 0}); // вгору
                }
                if (x > 1) {
                    possibleDirections.add(new int[]{0, -1}); // вліво
                }*/
                if (y < maze.getHeight() - 2) {
                    possibleDirections.add(new int[]{1, 0}); // вниз
                }
                if (x < maze.getWidth() - 2) {
                    possibleDirections.add(new int[]{0, 1});
                }

                if (!possibleDirections.isEmpty()) {
                    int[] dir = possibleDirections.get(r.nextInt(possibleDirections.size()));

                    int wallY = y + dir[0];
                    int wallX = x + dir[1];

                    maze.setValue(wallY, wallX, 0); // Ламаємо стіну
                }

            }
        }

    }

}
