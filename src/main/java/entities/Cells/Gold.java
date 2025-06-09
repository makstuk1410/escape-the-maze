package entities.Cells;

import gui.Game.GameScreen;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import managment.GameManager;
import managment.ScoreManager;

public class Gold implements Cell {

    private Image img;
    private boolean active = false;

    public Gold() {
        this.img = createGoldTile();
    }

    private Image createGoldTile() {
        int size = GameManager.getTileSize();

        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // фон
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, size, size);

        // жовте «золото»
        double diameter = size * 0.7;              // 70 % від тайла
        double radius   = diameter / 2;
        gc.setFill(Color.web("#FFCC00"));           // насичене золото
        gc.fillOval(
                (size - diameter) / 2,
                (size - diameter) / 2,
                diameter,
                diameter
        );

        // snapshot → Image
        WritableImage img = new WritableImage(size, size);
        canvas.snapshot(null, img);
        return img;
    }

    private Image createEmptyTile() {
        int size = GameManager.getTileSize();
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, size, size);

        WritableImage img = new WritableImage(size, size);
        canvas.snapshot(null, img);
        return img;
    }

    @Override
    public void doEffects() {
        if (active) return;          // уже зібране
        active = true;

        ScoreManager.addScore(10);
        GameScreen.updateScore();

        // міняємо зображення клітинки на порожнє
        this.img = createEmptyTile();
    }

    @Override
    public Image getImg() {
        return img;
    }
}
