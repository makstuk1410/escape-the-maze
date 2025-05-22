
package gui.Game;

import entities.Cells.Cell;
import entities.MazeObjects.Player;
import entities.Cells.Wall;

public class MovementController {
    private static final int TILE_SIZE = 80;

    public boolean movePlayer(Player player, int dx, int dy, int speed, Cell[][] maze) {
        if (dx == 0 && dy == 0) return false;

        int steps = speed;
        while (steps > 0) {
            double newX = player.getPositionX() + Integer.signum(dx) * steps;
            double newY = player.getPositionY() + Integer.signum(dy) * steps;
            double size = TILE_SIZE - 20;

            int startRow = (int) (newY / TILE_SIZE);
            int endRow = (int) ((newY + size - 1) / TILE_SIZE);
            int startCol = (int) (newX / TILE_SIZE);
            int endCol = (int) ((newX + size - 1) / TILE_SIZE);

            if (!hasCollision(startRow, endRow, startCol, endCol, maze)) {
                player.move(Integer.signum(dx) * steps, Integer.signum(dy) * steps);
                return true;
            }
            steps--;
        }
        return false;
    }

    private boolean hasCollision(int startRow, int endRow, int startCol, int endCol, Cell[][] maze) {
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                if (!isInBounds(row, col, maze)) return true;
                if (maze[row][col] instanceof Wall) return true;
            }
        }
        return false;
    }

    private boolean isInBounds(int row, int col, Cell[][] maze) {
        return row >= 0 && col >= 0 && row < maze.length && col < maze[0].length;
    }
}