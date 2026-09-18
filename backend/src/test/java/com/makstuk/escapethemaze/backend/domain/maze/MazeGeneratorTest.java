package com.makstuk.escapethemaze.backend.domain.maze;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayDeque;
import java.util.Queue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MazeGeneratorTest {

    @ParameterizedTest
    @EnumSource(GeneratorType.class)
    void createsOneConnectedSetOfInteriorPaths(GeneratorType generatorType) {
        MazeGrid grid = new MazeGrid(10, 10);

        MazeGeneratorFactory.create(generatorType, grid).createRandomMaze();

        boolean[][] visited = visitPaths(grid, 1, grid.getStartX());
        for (int y = 1; y < grid.getHeight() - 1; y++) {
            for (int x = 1; x < grid.getWidth() - 1; x++) {
                if (grid.getValue(y, x) == 0) {
                    assertThat(visited[y][x])
                            .as("%s path at (%s, %s) should be connected", generatorType, x, y)
                            .isTrue();
                }
            }
        }
    }

    private boolean[][] visitPaths(MazeGrid grid, int startY, int startX) {
        boolean[][] visited = new boolean[grid.getHeight()][grid.getWidth()];
        Queue<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{startY, startX});
        visited[startY][startX] = true;

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        while (!queue.isEmpty()) {
            int[] current = queue.remove();
            for (int[] direction : directions) {
                int nextY = current[0] + direction[0];
                int nextX = current[1] + direction[1];
                if (nextY >= 0 && nextY < grid.getHeight()
                        && nextX >= 0 && nextX < grid.getWidth()
                        && !visited[nextY][nextX]
                        && grid.getValue(nextY, nextX) == 0) {
                    visited[nextY][nextX] = true;
                    queue.add(new int[]{nextY, nextX});
                }
            }
        }
        return visited;
    }
}
