package algorithms;

import entities.MazeObjects.Maze;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class KruskalMazeGenerator extends MazeGenerator {

    public KruskalMazeGenerator(Maze maze) {
        super(maze);
    }

    private Map<String, String> parent = new HashMap<>();
    private Map<String, Integer> rank = new HashMap<>();

    @Override
    public void createRandomMaze() {
        fillMaze();
        List<int[]> walls = new ArrayList<>();

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
                String coords = i + " " + j;
                parent.put(coords, coords);
                rank.put(coords, 0);
                maze.setValue(i, j, 0);
            }
        }

        Random r = new Random();
        while (!walls.isEmpty()) {
            int[] wall = walls.remove(r.nextInt(walls.size()));
            int[] cell1, cell2;
            if (wall[0] % 2 == 0) {
                cell1 = new int[]{wall[0] - 1, wall[1]};
                cell2 = new int[]{wall[0] + 1, wall[1]};
            } else {
                cell1 = new int[]{wall[0], wall[1] - 1};
                cell2 = new int[]{wall[0], wall[1] + 1};
            }

            String root1 = find(cell1[0] + " " + cell1[1]);
            String root2 = find(cell2[0] + " " + cell2[1]);

            if (!root1.equals(root2)) {
                maze.setValue(wall[0], wall[1], 0);
                union(root1, root2);
            }
        }
    }

    private String find(String x) {
        while (!x.equals(parent.get(x))) {
            parent.put(x, parent.get(parent.get(x)));
            x = parent.get(x);
        }
        return x;
    }

    void union(String rootX, String rootY) {
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
