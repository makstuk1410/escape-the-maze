
package gui.Game;

import entities.Cells.Cell;
import entities.MazeObjects.Player;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import managment.GameConfig;

public class EffectProcessor {
    private final Cell[][] maze;
    private final Player player;
    private final Runnable onDeath;
    private final Timeline timeline;
    private final HealthBar healthBar;


    public EffectProcessor(Cell[][] maze, Player player, Runnable onDeathCallback, HealthBar healthBar) {
        this.maze = maze;
        this.player = player;
        this.onDeath = onDeathCallback;
        this.healthBar = healthBar;

        timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), e -> applyEffects()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void start() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }

    private void applyEffects() {
        if (player.isJumping()) return;

        double x = player.getPositionX();
        double y = player.getPositionY();
        double size = GameConfig.TILE_SIZE - 20;

        int startRow = (int) (y / GameConfig.TILE_SIZE);
        int endRow = (int) ((y + size - 1) / GameConfig.TILE_SIZE);
        int startCol = (int) (x / GameConfig.TILE_SIZE);
        int endCol = (int) ((x + size - 1) / GameConfig.TILE_SIZE);

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                if (isInBounds(row, col)) maze[row][col].doEffects();
            }
        }

        healthBar.update();
        
        if (player.getHealthPoints() <= 0) {
            onDeath.run();
        }
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && col >= 0 && row < maze.length && col < maze[0].length;
    }
}
