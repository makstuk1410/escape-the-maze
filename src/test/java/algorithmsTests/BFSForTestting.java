package algorithmsTests;

import entities.MazeObjects.Maze;
import java.util.LinkedList;
import java.util.Queue;

public class BFSForTestting {

    static public boolean[][] bfs(Maze maze) {
        int height = maze.getHeight();
        int width = maze.getWidth();
        boolean[][] visited = new boolean[height][width];

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{maze.getStartY(), maze.getStartX()}); // [y, x]
        visited[maze.getStartY()][maze.getStartX()] = true;

        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            int[] currentPosition = queue.poll();
            int y = currentPosition[0];
            int x = currentPosition[1];

            for (int i = 0; i < 4; i++) {
                int newY = y + dy[i];
                int newX = x + dx[i];

                if (newX >= 0 && newX < width && newY >= 0 && newY < height && !visited[newY][newX]) {
                    if (maze.getValue(newY, newX) == 0) {
                        visited[newY][newX] = true;
                        queue.add(new int[]{newY, newX}); // знову [y, x]
                    }
                }
            }
        }

        return visited;
    }
}
