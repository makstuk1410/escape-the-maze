package entities.Cells;

import java.net.URL;
import javafx.animation.PauseTransition;
import javafx.scene.image.Image;
import javafx.util.Duration;
import managment.GameManager;

public class Freeze implements Cell {

    private Image img;
    private static boolean active = false;

    public Freeze() {
        URL url = getClass().getResource("/entities/freeze.png");
        if (url == null) {
            System.err.println("Resource not found: /entities/freeze.png");
        } else {
            this.img = new Image(url.toExternalForm());
        }
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
