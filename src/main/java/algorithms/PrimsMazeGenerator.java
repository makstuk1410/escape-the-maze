package algorithms;

import entities.MazeObjects.Maze;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PrimsMazeGenerator extends MazeGenerator {

    public PrimsMazeGenerator(Maze maze) {
        super(maze);
    }

    @Override
    public void createRandomMaze() {
        fillMaze();
        List<int[]> notVisitedWalls = new ArrayList<>();
        int x = maze.getStartX();
        int y = maze.getStartY() + 1;
        maze.setValue(y, x, 0);
        addAllWallsToArray(notVisitedWalls, y, x);

        Random r = new Random();

        while (!notVisitedWalls.isEmpty()) {
            int[] wall = notVisitedWalls.remove(r.nextInt(notVisitedWalls.size()));

            int[] empty = null;
            int n = 0;
            for (int i = 0; i < 4; i++) {
                int nx = wall[1] + (directions[i][1] / 2);
                int ny = wall[0] + (directions[i][0] / 2);
                if (maze.getValue(ny, nx) == 0) {
                    if (empty == null) {
                        empty = new int[4];
                        empty[0] = wall[0] + (directions[i][0] / (-2));
                        empty[1] = wall[1] + (directions[i][1] / (-2));
                        empty[2] = wall[0];
                        empty[3] = wall[1];

                    } else {
                        n = 1;
                        break;
                    }
                }
            }
            if (n == 1) {
                continue;
            }

            maze.setValue(empty[2], empty[3], 0);
            if (inBounds(empty[0], empty[1])) {
                maze.setValue(empty[0], empty[1], 0);
                addAllWallsToArray(notVisitedWalls, empty[0], empty[1]);
            }

        }
    }

    private void addAllWallsToArray(List<int[]> walls, int y, int x) {
        for (int i = 0; i < 4; i++) {
            int nx = x + directions[i][1] / 2;
            int ny = y + directions[i][0] / 2;
            if (inBounds(ny, nx) && maze.getValue(ny, nx) == 1) {
                int[] wall = {ny, nx};
                walls.add(wall);
            }
        }
    }

}
