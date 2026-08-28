package entities.Tiles;

import game.GameState;

public class EmptyTile implements Tile {
    @Override
    public boolean isWalkable() {
        return true;
    }
    @Override
    public void onEnter(GameState gameState) {
    }
}
