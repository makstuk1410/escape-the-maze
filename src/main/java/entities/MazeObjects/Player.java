package entities.MazeObjects;

import java.net.URL;
import javafx.scene.image.Image;

public class Player {

    private int healthPoints;
    private int positionX;
    private int positionY;
    private Image img;
    private boolean isJumping = false;
    private double scale = 1.0;

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getScale() {
        return scale;
    }

    public boolean isJumping() {
        return isJumping;
    }

    public void setJumping(boolean jumping) {
        isJumping = jumping;
    }

    public Player(int y, int x) {
        this.positionY = y;
        this.positionX = x;
        this.healthPoints = 100;
        URL url = getClass().getResource("/entities/player.png");
        if (url == null) {
            System.err.println("⛔ Resource not found: /entities/player.png");
        } else {
            this.img = new Image(url.toExternalForm());
        }
    }

    public void setImage(String image) {
        URL url = getClass().getResource("/entities/" + image);
        if (url == null) {
            System.err.println("⛔ Resource not found: /entities/" + image);
        } else {
            this.img = new Image(url.toExternalForm());
        }
    }

    public void resetImage() {
        URL url = getClass().getResource("/entities/player.png");
        if (url == null) {
            System.err.println("⛔ Resource not found: /entities/player.png");
        } else {
            this.img = new Image(url.toExternalForm());
        }
    }

    public void getDamage(int healthPoints) {
        this.healthPoints -= healthPoints;
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public double getCenterX() {
        return positionX;
    }

    public double getCenterY() {
        return positionY;
    }

    public void move(double dx, double dy) {
        this.positionX += dx;
        this.positionY += dy;
    }

    public Image getImg() {
        return img;
    }

}
