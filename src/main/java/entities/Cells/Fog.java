package entities.Cells;

import gui.Game.GameScreen;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import managment.GameManager;

public class Fog implements Cell {

    private final Image img;
    private static boolean setFog = false;

    public Fog() {
        int width = GameManager.getTileSize();
        int height = GameManager.getTileSize();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pw = writableImage.getPixelWriter();

        // Малюємо світло-сірий фон
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pw.setColor(x, y, Color.DARKGRAY);
            }
        }

        this.img = writableImage;
    }

    public static void startFogAnimation() {
        if (setFog) {
            return;
        }
        setFog = true;
        final double maxScale = 80;
        final double durationMs = 4000;

        javafx.beans.property.DoubleProperty scaleProp = new javafx.beans.property.SimpleDoubleProperty(1.0);

        final long[] lastUpdateTime = {0};
        scaleProp.addListener((obs, oldVal, newVal) -> {
            long now = System.currentTimeMillis();
            if (now - lastUpdateTime[0] > 16) {
                drawCanvasBorders(newVal.doubleValue());
                lastUpdateTime[0] = now;
            }
        });

        Timeline fogTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(scaleProp, 1.0)),
                new KeyFrame(Duration.millis(durationMs / 4), new KeyValue(scaleProp, maxScale)),
                new KeyFrame(Duration.millis(durationMs / 4 * 3), new KeyValue(scaleProp, maxScale)),
                new KeyFrame(Duration.millis(durationMs), new KeyValue(scaleProp, 1.0))
        );

        fogTimeline.setOnFinished(e -> {
            drawCanvasBorders(0.0);
            setFog = false;
        });

        fogTimeline.play();
    }

    private static void drawCanvasBorders(double scale) {
        double thickness = 3 * scale;
        double width = GameScreen.fogCanvas.getWidth();
        double height = GameScreen.fogCanvas.getHeight();

        GameScreen.fogGC.clearRect(0, 0, width, height); 

        GameScreen.fogGC.setFill(Color.web("#666d6f")); 

        GameScreen.fogGC.fillRect(0, 0, width, thickness);
        GameScreen.fogGC.fillRect(0, height - thickness, width, thickness);
        GameScreen.fogGC.fillRect(0, 0, thickness, height);
        GameScreen.fogGC.fillRect(width - thickness, 0, thickness, height);
    }
    
    @Override
    public void doEffects() {

        startFogAnimation();
    }

    @Override
    public Image getImg() {
        return img;
    }

}
