package algorithms;

import entities.MazeObjects.MazeGrid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class KruskalMazeGenerator extends MazeGenerator {

    public KruskalMazeGenerator(MazeGrid maze) {
        super(maze);
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

        for (int i = 1; i < maze.getHeight() - 1; i += 2) {
            for (int j = 1; j < maze.getWidth() - 1; j += 2) {
                Position coords = new Position(i, j);
                parent.put(coords, coords);
                rank.put(coords, 0);
                maze.setValue(i, j, 0);
            }
        }

        while (!walls.isEmpty()) {
            int[] wall = walls.remove(random.nextInt(walls.size()));
            int[] cell1, cell2;
            if (wall[0] % 2 == 0) {
                cell1 = new int[]{wall[0] - 1, wall[1]};
                cell2 = new int[]{wall[0] + 1, wall[1]};
            } else {
                cell1 = new int[]{wall[0], wall[1] - 1};
                cell2 = new int[]{wall[0], wall[1] + 1};
            }

            Position root1 = find(new Position(cell1[0], cell1[1]), parent);
            Position root2 = find(new Position(cell2[0], cell2[1]), parent);

            if (!root1.equals(root2)) {
                maze.setValue(wall[0], wall[1], 0);
                union(root1, root2, parent, rank);
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
            Position rootX,
            Position rootY,
            Map<Position, Position> parent,
            Map<Position, Integer> rank
    ) {
        if (rootX.equals(rootY)) {
            return;
        }
        if (rank.get(rootX) < rank.get(rootY)) {
            parent.put(rootX, rootY);
        } else if (rank.get(rootX) > rank.get(rootY)) {
            parent.put(rootY, rootX);
        } else {
            parent.put(rootY, rootX);
            rank.put(rootX, rank.get(rootX) + 1);
        }
    }

}
