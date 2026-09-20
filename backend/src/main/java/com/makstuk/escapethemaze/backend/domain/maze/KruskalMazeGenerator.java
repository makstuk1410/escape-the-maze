package com.makstuk.escapethemaze.backend.domain.maze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class KruskalMazeGenerator extends MazeGenerator {

    public KruskalMazeGenerator(MazeGrid maze) {
        super(maze);
    }

    public KruskalMazeGenerator(MazeGrid maze, Random random) {
        super(maze, random);
    }

    private record Position(int y, int x) {
    }

    @Override
    public void createRandomMaze() {
        fillMaze();
        List<int[]> walls = new ArrayList<>();
        Map<Position, Position> parent = new HashMap<>();
        Map<Position, Integer> rank = new HashMap<>();

        for (int y = 1; y < maze.getHeight(); y += 2) {
            for (int x = 1; x < maze.getWidth(); x += 2) {
                if (x + 2 < maze.getWidth()) {
                    walls.add(new int[]{y, x + 1});
                }
                if (y + 2 < maze.getHeight()) {
                    walls.add(new int[]{y + 1, x});
                }
            }
        }

        for (int y = 1; y < maze.getHeight() - 1; y += 2) {
            for (int x = 1; x < maze.getWidth() - 1; x += 2) {
                Position position = new Position(y, x);
                parent.put(position, position);
                rank.put(position, 0);
                maze.setValue(y, x, 0);
            }
        }

        while (!walls.isEmpty()) {
            int[] wall = walls.remove(random.nextInt(walls.size()));
            int[] firstCell;
            int[] secondCell;
            if (wall[0] % 2 == 0) {
                firstCell = new int[]{wall[0] - 1, wall[1]};
                secondCell = new int[]{wall[0] + 1, wall[1]};
            } else {
                firstCell = new int[]{wall[0], wall[1] - 1};
                secondCell = new int[]{wall[0], wall[1] + 1};
            }

            Position firstRoot = find(new Position(firstCell[0], firstCell[1]), parent);
            Position secondRoot = find(new Position(secondCell[0], secondCell[1]), parent);
            if (!firstRoot.equals(secondRoot)) {
                maze.setValue(wall[0], wall[1], 0);
                union(firstRoot, secondRoot, parent, rank);
            }
        }
    }

    private Position find(Position position, Map<Position, Position> parent) {
        while (!position.equals(parent.get(position))) {
            parent.put(position, parent.get(parent.get(position)));
            position = parent.get(position);
        }
        return position;
    }

    private void union(
            Position firstRoot,
            Position secondRoot,
            Map<Position, Position> parent,
            Map<Position, Integer> rank) {
        if (rank.get(firstRoot) < rank.get(secondRoot)) {
            parent.put(firstRoot, secondRoot);
        } else if (rank.get(firstRoot) > rank.get(secondRoot)) {
            parent.put(secondRoot, firstRoot);
        } else {
            parent.put(secondRoot, firstRoot);
            rank.put(firstRoot, rank.get(firstRoot) + 1);
        }
    }
}
