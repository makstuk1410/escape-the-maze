package entities.Tiles;
import game.GameState;

public interface Tile {
    void onEnter(GameState gameState);
    boolean isWalkable();
}
