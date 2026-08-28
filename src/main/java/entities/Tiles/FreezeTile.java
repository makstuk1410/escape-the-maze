package entities.Tiles;

import game.GameState;
import management.GameConfig;


public class FreezeTile implements Tile {

	@Override
	public void onEnter(GameState gameState) {
		gameState.freezePlayer(GameConfig.FREEZE_DURATION_MS);
	}

	@Override
	public boolean isWalkable() {
		return true;
	}
}
