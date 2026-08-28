package entities.MazeObjects;

public class Player {

    private int healthPoints;
    private double positionX;
    private double positionY;
    private boolean isJumping = false;
    private double scale = 1.0;
    private int speed;

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

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
        this.speed = 5;
    }

    public void takeDamage(int healthPoints) {
        this.healthPoints -= healthPoints;
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public double getPositionX() {
        return positionX;
    }

    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }

    public void move(double dx, double dy) {
        this.positionX += dx;
        this.positionY += dy;
    }

}
