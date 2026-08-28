package entities.Tiles;

import game.GameState;

public class GoldTile implements Tile {

    boolean collected = false;
	
	public boolean isCollected() {
		return collected;
	}

	@Override
	public void onEnter(GameState gameState) {
		if (collected) {
            return;
        }

        collected = true;
        gameState.addScore(10);
	}

	@Override
	public boolean isWalkable() {
		return true;
	}
}
