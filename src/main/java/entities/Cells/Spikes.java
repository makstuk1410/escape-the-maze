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

public class Spikes implements Cell {

    private Image img;
    private static boolean active = false;

    public Spikes() {
        int size = GameConfig.TILE_SIZE;
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // 1️⃣ Фон
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, size, size);

        // 2️⃣ Малюємо 5 трикутників (шипи)
        gc.setFill(Color.DARKRED);
        int rows = 4;
        int cols = 4;

        double cellWidth = size / (double) cols;
        double cellHeight = size / (double) rows;

        double spikeScale = 0.6; // <= ось тут контролюємо розмір шипа

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = col * cellWidth;
                double y = row * cellHeight;

                // Центр клітинки
                double cx = x + cellWidth / 2;
                double cy = y + cellHeight / 2;

                // Ширина та висота шипа
                double spikeW = cellWidth * spikeScale;
                double spikeH = cellHeight * spikeScale;

                // Координати трикутника (вниз напрямком)
                gc.fillPolygon(
                        new double[]{cx, cx - spikeW / 2, cx + spikeW / 2},
                        new double[]{cy - spikeH / 2, cy + spikeH / 2, cy + spikeH / 2},
                        3
                );
            }
        }

        // 3️⃣ Snapshot → Image
        WritableImage img = new WritableImage(size, size);
        canvas.snapshot(null, img);
        this.img = img; // ⚠️ найважливіше!
    }

    @Override
    public void doEffects() {
        if (active) {
            return;
        }

        active = true;

        // 🔴 Змінюємо спрайт + знімаємо HP
        GameManager.getPlayer().setImage("red.png");
        GameManager.getPlayer().getDamage(25);

        // Через 0.5 сек — повертаємо звичайний вигляд
        PauseTransition toNormal = new PauseTransition(Duration.seconds(0.5));
        toNormal.setOnFinished(e -> GameManager.getPlayer().resetImage());

        // Через 1 сек — знову дозволяємо завдати шкоди
        PauseTransition done = new PauseTransition(Duration.seconds(1.0));
        done.setOnFinished(e -> active = false);

        toNormal.play();
        done.play();
    }

    public void stopEffects() {
        active = false;
        GameManager.getPlayer().resetImage();
    }

    @Override
    public Image getImg() {
        return img;
    }
}
