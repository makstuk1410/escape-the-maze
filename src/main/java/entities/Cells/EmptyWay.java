package entities.Cells;

import java.net.URL;
import javafx.scene.image.Image;

public class EmptyWay implements Cell {

    private Image img;

    public EmptyWay() {

        URL url = getClass().getResource("/entities/empty.png");
        if (url == null) {
            System.err.println("⛔ Resource not found: /entities/empty.png");
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
