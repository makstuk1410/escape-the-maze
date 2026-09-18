package com.makstuk.escapethemaze.backend.domain.maze;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BinaryTreeMazeGenerator extends MazeGenerator {

    public BinaryTreeMazeGenerator(MazeGrid maze) {
        super(maze);
    }

    public BinaryTreeMazeGenerator(MazeGrid maze, Random random) {
        super(maze, random);
    }

    @Override
    public void createRandomMaze() {
        fillMaze();
        for (int y = 1; y < maze.getHeight() - 1; y += 2) {
            for (int x = 1; x < maze.getWidth() - 1; x += 2) {
                maze.setValue(y, x, 0);
                List<int[]> possibleDirections = new ArrayList<>();
                if (y < maze.getHeight() - 2) {
                    possibleDirections.add(new int[]{1, 0});
                }
                if (x < maze.getWidth() - 2) {
                    possibleDirections.add(new int[]{0, 1});
                }

                if (!possibleDirections.isEmpty()) {
                    int[] direction = possibleDirections.get(random.nextInt(possibleDirections.size()));
                    maze.setValue(y + direction[0], x + direction[1], 0);
                }
            }
        }
    }
}
