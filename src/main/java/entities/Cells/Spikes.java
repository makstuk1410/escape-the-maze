package entities.Cells;

import java.net.URL;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.util.Duration;
import managment.GameManager;

public class Spikes implements Cell {

    private Image img;
    private Timeline damageTimeline;
    private static boolean active = false;

    public Spikes() {
        URL url = getClass().getResource("/entities/spikes.png");
        if (url == null) {
            System.err.println("Resource not found: /entities/spikes.png");
        } else {
            this.img = new Image(url.toExternalForm());
        }
    }

    @Override
    public void doEffects() {
        if (active) return;

        active = true;

        // 🔴 червоний
        GameManager.getPlayer().setImage("red.png");
        GameManager.getPlayer().getDamage(25);
        
        // Через 0.5 сек — скидаємо
        PauseTransition toNormal = new PauseTransition(Duration.seconds(0.5));
        toNormal.setOnFinished(e -> GameManager.getPlayer().resetImage());

        // Через 1 сек — знову дозволяємо ефект
        PauseTransition done = new PauseTransition(Duration.seconds(0.5));
        done.setOnFinished(e -> active = false);

        toNormal.play();
        done.play();
    }

    public void stopEffects() {
        if (damageTimeline != null) {
            damageTimeline.stop();
            damageTimeline = null;
        }
        active = false;
        GameManager.getPlayer().resetImage(); // на всяк випадок
    }

    @Override
    public Image getImg() {
        return img;
    }
}
