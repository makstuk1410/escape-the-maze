package entities.Tiles;

import game.GameState;
import management.GameConfig;

public class EndTile implements Tile {
	@Override
	public void onEnter(GameState gameState) {
		if (gameState.isGameWon()) {
			return;
		}

		gameState.addScore(GameConfig.END_SCORE);
        gameState.win();
	}

	@Override
	public boolean isWalkable() {
		return true;
	}
}
