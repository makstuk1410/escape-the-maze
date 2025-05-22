package entities.Cells;

import entities.Cells.Cell;
import java.net.URL;
import javafx.scene.image.Image;

public class Wall implements Cell {

    private Image img = null;

    public Wall() {
        URL url = getClass().getResource("/entities/wall.png");
        if (url == null) {
            System.err.println("⛔ Resource not found: /entities/wall.png");
        } else {
            this.img = new Image(url.toExternalForm());
        }
    }

    @Override
    public void doEffects() {
    }

    @Override
    public Image getImg() {
        return img;
    }
    
    

}
