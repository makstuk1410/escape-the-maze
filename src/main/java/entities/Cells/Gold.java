package entities.Cells;

import gui.Game.GameScreen;
import java.net.URL;
import javafx.scene.image.Image;
import managment.ScoreManager;

public class Gold implements Cell {

    private Image img;
    private boolean active = false;

    public Gold() {
        URL url = getClass().getResource("/entities/gold.png");
        if (url == null) {
            System.err.println("Resource not found: /entities/gold.png");
        } else {
            this.img = new Image(url.toExternalForm());
        }
    }

    @Override
    public void doEffects() {
        if (active) return;

        active = true;
        ScoreManager.addScore(10);
        GameScreen.updateScore();
        URL url = getClass().getResource("/entities/empty.png");
        if (url == null) {
            System.err.println("Resource not found: /entities/empty.png");
        } else {
            this.img = new Image(url.toExternalForm());
        }
        
    }

    @Override
    public Image getImg() {
        return img;
    }
}
