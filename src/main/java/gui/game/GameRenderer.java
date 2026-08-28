package gui.game;

import entities.MazeObjects.Player;
import entities.Tiles.Tile;
import javafx.scene.canvas.GraphicsContext;
import management.GameConfig;

public class GameRenderer {
    private final TileRenderer tileRenderer = new TileRenderer();
    private final PlayerRenderer playerRenderer = new PlayerRenderer();

    public void drawMaze(GraphicsContext gc, 
            Tile[][] maze, 
            double offsetX, 
            double offsetY, 
            double canvasWidth, 
            double canvasHeight
    ) {
        int startCol = (int) (offsetX / GameConfig.TILE_SIZE);
        int startRow = (int) (offsetY / GameConfig.TILE_SIZE);

        int maxCol = startCol + 11 + (offsetX % GameConfig.TILE_SIZE != 0 ? 1 : 0);
        int maxRow = startRow + 11 + (offsetY % GameConfig.TILE_SIZE != 0 ? 1 : 0);

        for (int row = startRow; row < maxRow; row++) {
            for (int col = startCol; col < maxCol; col++) {
                if (row < 0 || col < 0 || row >= maze.length || col >= maze[0].length) {
                    continue;
                }

                double x = col * GameConfig.TILE_SIZE - offsetX;
                double y = row * GameConfig.TILE_SIZE - offsetY;

                tileRenderer.draw(
                        gc,
                        maze[row][col],
                        x,
                        y
                );
            }
        }
    }

    public void drawPlayer(GraphicsContext gc, Player player, double offsetX, double offsetY) {
        playerRenderer.draw(
                gc,
                player,
                offsetX,
                offsetY
        );
    }
}
