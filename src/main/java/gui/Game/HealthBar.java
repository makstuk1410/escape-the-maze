
package gui.Game;

import entities.MazeObjects.Player;
import managment.GameManager;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class HealthBar extends StackPane {

    private final Rectangle backgroundBar;
    private final Rectangle healthBar;

    private final int width = 200;
    private final int height = 38;
    private final Player player;

    public HealthBar() {
        player = GameManager.getPlayer();

        backgroundBar = new Rectangle(width, height, Color.DARKRED);
        backgroundBar.setStroke(Color.WHITE);
        backgroundBar.setStrokeWidth(3);

        healthBar = new Rectangle(width-4, height-4, Color.LIMEGREEN);
        

        this.getChildren().addAll(backgroundBar, healthBar);

        update(); 
    }

    public void update() {
        int hp = player.getHealthPoints();
        double ratio = Math.max(0, Math.min(1.0, hp / 100.0));

        healthBar.setWidth(width * ratio);
        
        StackPane.setMargin(healthBar, new Insets(0, width * (1 - ratio), 0, 0));
    }
}
