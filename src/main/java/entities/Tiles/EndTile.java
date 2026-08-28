package entities.Tiles;

import game.GameState;

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

		gameState.addScore(100);
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
