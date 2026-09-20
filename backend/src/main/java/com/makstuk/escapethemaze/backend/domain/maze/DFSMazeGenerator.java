package com.makstuk.escapethemaze.backend.domain.maze;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class DFSMazeGenerator extends MazeGenerator {

    public DFSMazeGenerator(MazeGrid maze) {
        super(maze);
    }

    public DFSMazeGenerator(MazeGrid maze, Random random) {
        super(maze, random);
    }

    @Override
    public void createRandomMaze() {
        fillMaze();

        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{maze.getStartY() + 1, maze.getStartX()});
        maze.setValue(maze.getStartY(), maze.getStartX(), 0);
        maze.setValue(maze.getStartY() + 1, maze.getStartX(), 0);

        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int y = current[0];
            int x = current[1];
            List<int[]> neighbors = new ArrayList<>();

            for (int[] direction : directions) {
                int nextY = y + direction[0];
                int nextX = x + direction[1];
                if (inBounds(nextY, nextX) && maze.getValue(nextY, nextX) == 1) {
                    neighbors.add(new int[]{nextY, nextX, direction[0], direction[1]});
                }
            }

            if (neighbors.isEmpty()) {
                stack.pop();
                continue;
            }

            int[] chosen = neighbors.get(random.nextInt(neighbors.size()));
            int nextY = chosen[0];
            int nextX = chosen[1];
            int deltaY = chosen[2];
            int deltaX = chosen[3];
            maze.setValue(y + deltaY / 2, x + deltaX / 2, 0);
            maze.setValue(nextY, nextX, 0);
            stack.push(new int[]{nextY, nextX});
        }
    }
}
