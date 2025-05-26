
package entities.Cells;

import gui.Game.GameScreen;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;


public class Fog implements Cell {

    private final Image img;
    
    public Fog(){
        int width = GameScreen.TILE_SIZE;
        int height = GameScreen.TILE_SIZE;
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
    
    
    @Override
    public void doEffects() {
        
        GameScreen.startFogAnimation();
    }

    @Override
    public Image getImg() {
        return img;
    }
    
}
