package entities.Tiles;

import game.GameState;
public class Wall implements Tile {
    @Override
    public boolean isWalkable() {
        return false;
    }
    
    @Override
    public void onEnter(GameState gameState) {}
}
