
package gui.game;

import entities.Tiles.Tile;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import management.GameConfig;
import game.GameState; 

public class EffectProcessor {
    private final Tile[][] tiles;
    private final Runnable onDeath;
    private final Timeline timeline;
    private final GameState gameState;


    public EffectProcessor(Tile[][] tiles, GameState gameState, Runnable onDeathCallback) {
        this.tiles = tiles;
        this.gameState = gameState;
        this.onDeath = onDeathCallback;

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
        if (gameState.getPlayer().isJumping()) return;

        double x = gameState.getPlayer().getPositionX();
        double y = gameState.getPlayer().getPositionY();
        double size = GameConfig.PLAYER_SIZE;

        int startRow = (int) (y / GameConfig.TILE_SIZE);
        int endRow = (int) ((y + size - 1) / GameConfig.TILE_SIZE);
        int startCol = (int) (x / GameConfig.TILE_SIZE);
        int endCol = (int) ((x + size - 1) / GameConfig.TILE_SIZE);

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                if (isInBounds(row, col)) tiles[row][col].onEnter(gameState);
            }
        }

        if (gameState.getPlayer().getHealthPoints() <= 0) {
            onDeath.run();
        }
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && col >= 0 && row < tiles.length && col < tiles[0].length;
    }
}
