package gui.Game;

import entities.MazeObjects.Player;
import entities.Tiles.Tile;
import entities.Tiles.Wall;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import managment.GameConfig;
import game.GameState;

import java.util.Set;

public class PlayerController {

    private final Player player;

    public PlayerController(Player player) {
        this.player = player;
    }

    public void move(
            Set<KeyCode> pressedKeys,
            Tile[][] maze,
            GameState gameState
    ) {
        int dx = 0;
        int dy = 0;

        if (pressedKeys.contains(KeyCode.W)) {
            dy--;
        }

        if (pressedKeys.contains(KeyCode.S)) {
            dy++;
        }

        if (pressedKeys.contains(KeyCode.A)) {
            dx--;
        }

        if (pressedKeys.contains(KeyCode.D)) {
            dx++;
        }

        movePlayer(dx, 0, maze, gameState);
        movePlayer(0, dy, maze, gameState);
    }

    private boolean movePlayer(
            int dx,
            int dy,
            Tile[][] maze,
            GameState gameState
    ) {
        if (dx == 0 && dy == 0) {
            return false;
        }

        int speed = gameState.getPlayerSpeed();

        for (int step = speed; step > 0; step--) {

            double newX = player.getPositionX()
                    + Integer.signum(dx) * step;

            double newY = player.getPositionY()
                    + Integer.signum(dy) * step;

            double size = GameConfig.TILE_SIZE - 20;

            int startRow = (int) (newY / GameConfig.TILE_SIZE);
            int endRow = (int) (
                    (newY + size - 1) / GameConfig.TILE_SIZE
            );

            int startCol = (int) (newX / GameConfig.TILE_SIZE);
            int endCol = (int) (
                    (newX + size - 1) / GameConfig.TILE_SIZE
            );

            if (!hasCollision(
                    startRow,
                    endRow,
                    startCol,
                    endCol,
                    maze
            )) {
                player.move(
                        Integer.signum(dx) * step,
                        Integer.signum(dy) * step
                );

                return true;
            }
        }

        return false;
    }

    private boolean hasCollision(
            int startRow,
            int endRow,
            int startCol,
            int endCol,
            Tile[][] maze
    ) {
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {

                if (!isInBounds(row, col, maze)) {
                    return true;
                }

                if (maze[row][col] instanceof Wall) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isInBounds(
            int row,
            int col,
            Tile[][] maze
    ) {
        return row >= 0
                && col >= 0
                && row < maze.length
                && col < maze[0].length;
    }

    public void jump() {

        if (player.isJumping()) {
            return;
        }

        player.setJumping(true);

        DoubleProperty scale =
                new SimpleDoubleProperty(1.0);

        scale.addListener((obs, oldValue, newValue) ->
                player.setScale(newValue.doubleValue())
        );

        Timeline jumpAnimation = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(scale, 1.0)
                ),
                new KeyFrame(
                        Duration.millis(
                                GameConfig.JUMP_DURATION_MS / 2.0
                        ),
                        new KeyValue(
                                scale,
                                GameConfig.JUMP_MAX_SCALE
                        )
                ),
                new KeyFrame(
                        Duration.millis(
                                GameConfig.JUMP_DURATION_MS
                        ),
                        new KeyValue(scale, 1.0)
                )
        );

        jumpAnimation.setOnFinished(event -> {
            player.setScale(1.0);
            player.setJumping(false);
        });

        jumpAnimation.play();
    }
}