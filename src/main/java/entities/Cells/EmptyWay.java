package entities.Cells;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import managment.GameConfig;

public class EmptyWay implements Cell {

    private Image img;

    public EmptyWay() {
        int width = GameConfig.TILE_SIZE;
        int height = GameConfig.TILE_SIZE;
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pw = writableImage.getPixelWriter();

        // Малюємо світло-сірий фон
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pw.setColor(x, y, Color.LIGHTGRAY);
            }
        }

        this.img = writableImage;
    }

    @Override
    public void doEffects() {}

    @Override
    public Image getImg() {
        return img;
    }
}
