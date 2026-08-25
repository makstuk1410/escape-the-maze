package entities.Cells;

import javafx.animation.PauseTransition;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import managment.GameConfig;
import managment.GameManager;

public class Freeze implements Cell {

    private Image img;
    private static boolean active = false;

    public Freeze() {
        int size = GameConfig.TILE_SIZE;
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // 1️⃣ Фон — світло-блакитний
        gc.setFill(Color.web("#e0f7fa")); // блакитний, холодний
        gc.fillRect(0, 0, size, size);

        // 2️⃣ Сніжинка/кристал — простий хрест
        gc.setStroke(Color.web("#00acc1")); // темніший блакитний
        gc.setLineWidth(2);

        double center = size / 2.0;
        double len = size * 0.25;

        // Вертикаль
        gc.strokeLine(center, center - len, center, center + len);
        // Горизонталь
        gc.strokeLine(center - len, center, center + len, center);
        // Діагоналі
        gc.strokeLine(center - len, center - len, center + len, center + len);
        gc.strokeLine(center - len, center + len, center + len, center - len);

        // 3️⃣ Snapshot → Image
        WritableImage image = new WritableImage(size, size);
        canvas.snapshot(null, image);
        this.img = image;
    }

    @Override
    public void doEffects() {
        if (active) {
            return;
        }

        active = true;

        // 🔴 червоний
        GameManager.getPlayer().setImage("blue.png");
        GameManager.speed = 2;

        PauseTransition activeFalse = new PauseTransition(Duration.seconds(1));
        activeFalse.setOnFinished(e -> {
            active = false;
        });

        PauseTransition toNormal = new PauseTransition(Duration.seconds(3));
        toNormal.setOnFinished(e -> {
            GameManager.getPlayer().resetImage();
            GameManager.speed = 5;
        });

        activeFalse.play();
        toNormal.play();
    }

    @Override
    public Image getImg() {
        return img;
    }

}
