
package entities;


public class Player {
    private int healthPoints;
    private int positionX;
    private int positionY;
    
    public Player(int y, int x){
        this.positionY = y;
        this.positionX = x;
        this.healthPoints = 100;
    }
    
    public void getDamage(int healthPoints){
        this.healthPoints = this.getHealthPoints() - healthPoints;
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
    
    
}
