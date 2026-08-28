package entities.Tiles;

import game.GameState;
import management.GameConfig;

public class EndTile implements Tile {
	private Runnable onReached;

	public void setOnReached(Runnable onReached) {
		this.onReached = onReached;
	}

	@Override
	public void onEnter(GameState gameState) {
		if (gameState.isGameWon()) {
			return;
		}

		gameState.addScore(GameConfig.END_SCORE);
        gameState.win();
		if (onReached != null) {
			onReached.run();
		}
	}

	@Override
	public boolean isWalkable() {
		return true;
	}
}
