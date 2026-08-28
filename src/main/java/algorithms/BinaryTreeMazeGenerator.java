package algorithms;

import entities.MazeObjects.MazeGrid;
import java.util.ArrayList;
import java.util.List;

public class BinaryTreeMazeGenerator extends MazeGenerator {

    public BinaryTreeMazeGenerator(MazeGrid maze) {
        super(maze);
    }

    @Override
    public void createRandomMaze() {
        fillMaze();
        for (int y = 1; y < maze.getHeight() - 1; y += 2) {
            for (int x = 1; x < maze.getWidth() - 1; x += 2) {
                maze.setValue(y, x, 0);

                List<int[]> possibleDirections = new ArrayList<>();
                if (y < maze.getHeight() - 2) {
                    possibleDirections.add(new int[]{1, 0}); // вниз
                }
                if (x < maze.getWidth() - 2) {
                    possibleDirections.add(new int[]{0, 1});
                }

                if (!possibleDirections.isEmpty()) {
                    int[] dir = possibleDirections.get(random.nextInt(possibleDirections.size()));

                    int wallY = y + dir[0];
                    int wallX = x + dir[1];

                    maze.setValue(wallY, wallX, 0); // Ламаємо стіну
                }

            }
        }

    }

}
