package game.effects;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import management.GameConfig;

public class FogEffect {

    private static final double MAX_SCALE = 80;
    private final GraphicsContext graphics;

    private boolean active = false;

    public FogEffect(GraphicsContext graphics) {
        this.graphics = graphics;
    }

    public void start() {
        if (active) {
            return;
        }

        active = true;

        DoubleProperty scale =
                new SimpleDoubleProperty(1.0);

        scale.addListener((obs, oldValue, newValue) ->
                drawBorders(newValue.doubleValue())
        );

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(scale, 1.0)
                ),
                new KeyFrame(
                        Duration.millis(GameConfig.FOG_DURATION_MS / 4.0),
                        new KeyValue(scale, MAX_SCALE)
                ),
                new KeyFrame(
                        Duration.millis(GameConfig.FOG_DURATION_MS * 3 / 4.0),
                        new KeyValue(scale, MAX_SCALE)
                ),
                new KeyFrame(
                        Duration.millis(GameConfig.FOG_DURATION_MS),
                        new KeyValue(scale, 1.0)
                )
        );

        timeline.setOnFinished(event -> {
            clear();
            active = false;
        });

        timeline.play();
    }

    private void drawBorders(double scale) {
        double thickness = 3 * scale;

        double width = graphics.getCanvas().getWidth();
        double height = graphics.getCanvas().getHeight();

        clear();

        graphics.setFill(Color.web("#666d6f"));

        graphics.fillRect(0, 0, width, thickness);
        graphics.fillRect(0, height - thickness, width, thickness);
        graphics.fillRect(0, 0, thickness, height);
        graphics.fillRect(width - thickness, 0, thickness, height);
    }

    private void clear() {
        double width = graphics.getCanvas().getWidth();
        double height = graphics.getCanvas().getHeight();

        graphics.clearRect(0, 0, width, height);
    }
}