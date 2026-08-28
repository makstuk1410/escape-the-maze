package gui.game;

import entities.MazeObjects.Player;
import management.GameConfig;

public class Camera {

    private final double width;
    private final double height;

    private double offsetX;
    private double offsetY;

    public Camera(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public void update(Player player, int mazeWidth, int mazeHeight) {
        double centerX = width / 2;
        double centerY = height / 2;

        double targetX =
                player.getPositionX()
                - centerX
                + GameConfig.TILE_SIZE / 2.0;

        double targetY =
                player.getPositionY()
                - centerY
                + GameConfig.TILE_SIZE / 2.0;

        double worldWidth =
                mazeWidth * GameConfig.TILE_SIZE;

        double worldHeight =
                mazeHeight * GameConfig.TILE_SIZE;

        offsetX = clamp(
                targetX,
                0,
                worldWidth - width
        );

        offsetY = clamp(
                targetY,
                0,
                worldHeight - height
        );
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}