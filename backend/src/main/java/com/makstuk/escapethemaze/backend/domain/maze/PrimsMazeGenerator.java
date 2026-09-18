package com.makstuk.escapethemaze.backend.domain.maze;

import java.util.ArrayList;
import java.util.List;

public class PrimsMazeGenerator extends MazeGenerator {

    public PrimsMazeGenerator(MazeGrid maze) {
        super(maze);
    }

    @Override
    public void createRandomMaze() {
        fillMaze();
        List<int[]> unvisitedWalls = new ArrayList<>();
        int x = maze.getStartX();
        int y = maze.getStartY() + 1;
        maze.setValue(y, x, 0);
        addAllWalls(unvisitedWalls, y, x);

        while (!unvisitedWalls.isEmpty()) {
            int[] wall = unvisitedWalls.remove(random.nextInt(unvisitedWalls.size()));
            int[] empty = null;
            boolean hasTwoOpenSides = false;

            for (int[] direction : directions) {
                int nextX = wall[1] + direction[1] / 2;
                int nextY = wall[0] + direction[0] / 2;
                if (maze.getValue(nextY, nextX) == 0) {
                    if (empty == null) {
                        empty = new int[]{
                            wall[0] - direction[0] / 2,
                            wall[1] - direction[1] / 2,
                            wall[0],
                            wall[1]
                        };
                    } else {
                        hasTwoOpenSides = true;
                        break;
                    }
                }
            }

            if (hasTwoOpenSides || empty == null) {
                continue;
            }

            maze.setValue(empty[2], empty[3], 0);
            if (inBounds(empty[0], empty[1])) {
                maze.setValue(empty[0], empty[1], 0);
                addAllWalls(unvisitedWalls, empty[0], empty[1]);
            }
        }
    }

    private void addAllWalls(List<int[]> walls, int y, int x) {
        for (int[] direction : directions) {
            int nextX = x + direction[1] / 2;
            int nextY = y + direction[0] / 2;
            if (inBounds(nextY, nextX) && maze.getValue(nextY, nextX) == 1) {
                walls.add(new int[]{nextY, nextX});
            }
        }
    }
}
