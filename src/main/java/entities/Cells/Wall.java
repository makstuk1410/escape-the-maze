package entities.Cells;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import managment.GameManager;

public class Wall implements Cell {

    private Image img = null;

    public Wall() {
        int width = GameManager.getTileSize();
        int height = GameManager.getTileSize();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pw = writableImage.getPixelWriter();

        // Малюємо світло-сірий фон
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pw.setColor(x, y, Color.web("#3D90D7"));
            }
        }

        this.img = writableImage;
    }

    @Override
    public void doEffects() {
    }

    @Override
    public Image getImg() {
        return img;
    }
    
    

}
