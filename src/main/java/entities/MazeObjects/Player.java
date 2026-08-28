package entities.MazeObjects;

public class Player {

    private int healthPoints;
    private int positionX;
    private int positionY;
    private boolean isJumping = false;
    private double scale = 1.0;
    private int speed;

    public enum PlayerEffect {
        NONE,
        DAMAGED,
        FROZEN
    }

    private PlayerEffect effect = PlayerEffect.NONE;

    public PlayerEffect getEffect() {
        return effect;
    }

    public void setEffect(PlayerEffect effect) {
        this.effect = effect;
    }

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

}
