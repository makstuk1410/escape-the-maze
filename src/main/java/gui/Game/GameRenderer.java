package gui.Game;

import entities.Cells.Cell;
import entities.MazeObjects.Player;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class GameRenderer {

    private static final int TILE_SIZE = 80;

    public void drawMaze(GraphicsContext gc, Cell[][] maze, double offsetX, double offsetY, double canvasWidth, double canvasHeight) {
        int startCol = (int) (offsetX / TILE_SIZE);
        int startRow = (int) (offsetY / TILE_SIZE);

        int maxCol = startCol + 11 + (offsetX % TILE_SIZE != 0 ? 1 : 0);
        int maxRow = startRow + 11 + (offsetY % TILE_SIZE != 0 ? 1 : 0);

        for (int row = startRow; row < maxRow; row++) {
            for (int col = startCol; col < maxCol; col++) {
                if (row < 0 || col < 0 || row >= maze.length || col >= maze[0].length) {
                    continue;
                }
                Image img = maze[row][col].getImg();
                if (img == null) {
                    continue;
                }

                double x = col * TILE_SIZE - offsetX;
                double y = row * TILE_SIZE - offsetY;

                double visibleX = Math.max(0, -x);
                double visibleY = Math.max(0, -y);
                double drawWidth = Math.min(TILE_SIZE - visibleX, canvasWidth - x);
                double drawHeight = Math.min(TILE_SIZE - visibleY, canvasHeight - y);

                gc.drawImage(img, visibleX, visibleY, drawWidth, drawHeight,
                        x + visibleX, y + visibleY, drawWidth, drawHeight);
            }
        }
    }

    public void drawPlayer(GraphicsContext gc, Player player, double offsetX, double offsetY) {
        double scale = player.getScale();
        double baseSize = TILE_SIZE - 20;
        double drawSize = baseSize * scale;

        double screenX = player.getPositionX() - offsetX + (baseSize - drawSize) / 2;
        double screenY = player.getPositionY() - offsetY + (baseSize - drawSize) / 2;

        gc.drawImage(player.getImg(), screenX, screenY, drawSize, drawSize);
    }
}
