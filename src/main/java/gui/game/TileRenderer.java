package gui.game;

import entities.Tiles.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import management.GameConfig;

public class TileRenderer {

    public void draw(
            GraphicsContext gc,
            Tile tile,
            double x,
            double y
    ) {
        if (tile instanceof Wall) {
            drawWall(gc, x, y);
        } else if (tile instanceof GoldTile gold) {
            drawGold(gc, gold, x, y);
        } else if (tile instanceof SpikesTile) {
            drawSpikes(gc, x, y);
        } else if (tile instanceof FreezeTile) {
            drawFreeze(gc, x, y);
        } else if (tile instanceof FogTile) {
            drawFog(gc, x, y);
        } else if (tile instanceof EndTile) {
            drawEnd(gc, x, y);
        } else {
            drawEmpty(gc, x, y);
        }
    }

    private void drawEmpty(
            GraphicsContext gc,
            double x,
            double y
    ) {
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(
                x,
                y,
                GameConfig.TILE_SIZE,
                GameConfig.TILE_SIZE
        );
    }

    private void drawWall(
            GraphicsContext gc,
            double x,
            double y
    ) {
        gc.setFill(Color.web("#3D90D7"));
        gc.fillRect(
                x,
                y,
                GameConfig.TILE_SIZE,
                GameConfig.TILE_SIZE
        );
    }

    private void drawGold(
            GraphicsContext gc,
            GoldTile tile,
            double x,
            double y
    ) {
        drawEmpty(gc, x, y);

        if (!tile.isCollected()) {
            double size = GameConfig.TILE_SIZE * 0.7;

            gc.setFill(Color.web("#FFCC00"));
            gc.fillOval(
                    x + (GameConfig.TILE_SIZE - size) / 2,
                    y + (GameConfig.TILE_SIZE - size) / 2,
                    size,
                    size
            );
        }
    }

    private void drawSpikes(
            GraphicsContext gc,
            double x,
            double y
    ) {
        drawEmpty(gc, x, y);

        gc.setFill(Color.DARKRED);

        int rows = 4;
        int cols = 4;

        double cellWidth =
                GameConfig.TILE_SIZE / (double) cols;

        double cellHeight =
                GameConfig.TILE_SIZE / (double) rows;

        double scale = 0.6;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {

                double cx =
                        x + col * cellWidth + cellWidth / 2;

                double cy =
                        y + row * cellHeight + cellHeight / 2;

                double width = cellWidth * scale;
                double height = cellHeight * scale;

                gc.fillPolygon(
                        new double[]{
                                cx,
                                cx - width / 2,
                                cx + width / 2
                        },
                        new double[]{
                                cy - height / 2,
                                cy + height / 2,
                                cy + height / 2
                        },
                        3
                );
            }
        }
    }

    private void drawFreeze(
            GraphicsContext gc,
            double x,
            double y
    ) {
        gc.setFill(Color.web("#e0f7fa"));
        gc.fillRect(
                x,
                y,
                GameConfig.TILE_SIZE,
                GameConfig.TILE_SIZE
        );

        gc.setStroke(Color.web("#00acc1"));
        gc.setLineWidth(2);

        double centerX = x + GameConfig.TILE_SIZE / 2.0;
        double centerY = y + GameConfig.TILE_SIZE / 2.0;
        double length = GameConfig.TILE_SIZE * 0.25;

        gc.strokeLine(
                centerX,
                centerY - length,
                centerX,
                centerY + length
        );

        gc.strokeLine(
                centerX - length,
                centerY,
                centerX + length,
                centerY
        );

        gc.strokeLine(
                centerX - length,
                centerY - length,
                centerX + length,
                centerY + length
        );

        gc.strokeLine(
                centerX - length,
                centerY + length,
                centerX + length,
                centerY - length
        );
    }

    private void drawFog(
            GraphicsContext gc,
            double x,
            double y
    ) {
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(
                x,
                y,
                GameConfig.TILE_SIZE,
                GameConfig.TILE_SIZE
        );
    }

    private void drawEnd(
            GraphicsContext gc,
            double x,
            double y
    ) {
        drawEmpty(gc, x, y);

        gc.setFill(Color.LIMEGREEN);

        double size = GameConfig.TILE_SIZE * 0.6;

        gc.fillOval(
                x + (GameConfig.TILE_SIZE - size) / 2,
                y + (GameConfig.TILE_SIZE - size) / 2,
                size,
                size
        );
    }
}