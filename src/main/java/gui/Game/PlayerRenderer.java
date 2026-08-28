package gui.Game;

import entities.MazeObjects.Player;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import managment.GameConfig;

public class PlayerRenderer {
    private static final Image PLAYER_IMAGE =
            new Image(
                PlayerRenderer.class.getResource(
                    "/entities/player.png"
                ).toExternalForm()
            );
    

    public void draw(
            GraphicsContext gc,
            Player player,
            double offsetX,
            double offsetY
    ) {
        double scale = player.getScale();

        double baseSize = GameConfig.TILE_SIZE - 20;
        double drawSize = baseSize * scale;

        double screenX =
                player.getPositionX()
                - offsetX
                + (baseSize - drawSize) / 2;

        double screenY =
                player.getPositionY()
                - offsetY
                + (baseSize - drawSize) / 2;

        gc.drawImage(
                PLAYER_IMAGE,
                screenX,
                screenY,
                drawSize,
                drawSize
        );
    }
}

