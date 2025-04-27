package algorithmsTests;

import entities.Maze;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RandomMazeGeneratorTest {

    private Maze maze = new Maze(2, 2);
    
    @BeforeEach
    public void setUp() {
        maze.create();
    }
    
    @Test
    public void testMazeIsFullyConnected() {
        boolean[][] visited = BFSForTestting.bfs(maze);

        for (int i = 0; i < maze.getHeight(); i++) {
            for (int j = 0; j < maze.getWidth(); j++) {
                if (maze.getValue(i, j) == 0) {
                    assertEquals(true, visited[i][j]);
                }
                if (maze.getValue(i, j) == 1) {
                    assertEquals(false, visited[i][j]);
                }
            }
        }
    }

    @Test
    public void testMazeForCorrectWallsAndWays() {
        int[] dx = {0, 1, 1};
        int[] dy = {1, 1, 0};
        for (int i = 0; i < maze.getHeight() - 1; i++) {
            for (int j = 0; j < maze.getWidth() - 1; j++) {
                int first = maze.getValue(i, j);
                int count = 0;
                for (int w = 0; w < 3; w++) {
                    if (maze.getValue(i + dy[w], j + dx[w]) == first) {
                        count++;
                    }
                }
                assertNotEquals(3, count);
            }
        }
    }

    @Test
    public void testMazeForSingleElements() {
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};
        for (int i = 1; i < maze.getHeight() - 1; i++) {
            for (int j = 1; j < maze.getWidth() - 1; j++) {
                int first = maze.getValue(i, j);
                int count = 0;
                for (int w = 0; w < 4; w++) {
                    if (maze.getValue(i + dy[w], j + dx[w]) != first) {
                        count++;
                    }
                }
                assertNotEquals(4, count);
            }
        }
    }

    @Test
    public void testIfWidthAndHeightAreOddNumbers() {
        int a = maze.getHeight() % 2; //must be an odd number
        assertEquals(1, a);

        int b = maze.getWidth() % 2;
        assertEquals(1, b);
    }

    @Test
    public void testForOnlyOneStartAndEndPoints() {
        int rows = maze.getHeight();
        int cols = maze.getWidth();
        int entryExitCount = 0;

        for (int col = 0; col < cols; col++) {
            if (maze.getValue(0, col) == 0) {
                entryExitCount++;
            }
            if (maze.getValue(rows - 1, col) == 0) {
                entryExitCount++;
            }
        }

        for (int row = 1; row < rows - 1; row++) {
            if (maze.getValue(row, 0) == 0) {
                entryExitCount++;
            }
            if (maze.getValue(row, cols - 1) == 0) {
                entryExitCount++;
            }
        }
        assertEquals(2, entryExitCount);
    }

    @Test
    public void testForPossibleZeroesInTheCorners() {
        int [] x = {0, maze.getWidth()-1, maze.getWidth()-1, 0};
        int [] y = {0, maze.getHeight()-1, 0, maze.getHeight()-1};
        for(int i = 0; i < 4; i++){
            assertEquals(1, maze.getValue(y[i], x[i]));
        }
    }
}
