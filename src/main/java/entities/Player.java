/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

/**
 *
 * @author maks
 */
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
        this.healthPoints = this.healthPoints - healthPoints;
    }
    
    
}
