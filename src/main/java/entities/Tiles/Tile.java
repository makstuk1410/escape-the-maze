package entities.Tiles;
import game.GameState;

public interface Tile {
    public void onEnter(GameState gameState);
    boolean isWalkable();
}
