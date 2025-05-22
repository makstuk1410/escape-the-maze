package algorithms;

import entities.MazeObjects.Maze;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class DFSMazeGenerator extends MazeGenerator {

    public DFSMazeGenerator(Maze maze) {
        super(maze);
    }


    @Override
    public void createRandomMaze() {
        fillMaze();

        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{maze.getStartY() + 1, maze.getStartX()});
        maze.setValue(maze.getStartY(), maze.getStartX(), 0);
        maze.setValue(maze.getStartY() + 1, maze.getStartX(), 0);

        
        
        
        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int y = current[0];
            int x = current[1];

            List<int[]> neighbors = new ArrayList<>();

            for (int[] dir : directions) {
                int ny = y + dir[0];
                int nx = x + dir[1];

                if (inBounds(ny, nx) && maze.getValue(ny, nx) == 1) {
                    neighbors.add(new int[]{ny, nx, dir[0], dir[1]});
                }
            }

            if (!neighbors.isEmpty()) {
                int[] chosen = neighbors.get(new Random().nextInt(neighbors.size()));
                int ny = chosen[0];
                int nx = chosen[1];
                int dy = chosen[2];
                int dx = chosen[3];

                maze.setValue(y + dy / 2, x + dx / 2, 0);
                maze.setValue(ny, nx, 0);

                stack.push(new int[]{ny, nx});
            } else {
                stack.pop();
            }
        }
    }

}
