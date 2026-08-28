package entities.Tiles;

import game.GameState;
import management.GameConfig;

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
		gameState.addScore(GameConfig.GOLD_SCORE);
	}

	@Override
	public boolean isWalkable() {
		return true;
	}
}
