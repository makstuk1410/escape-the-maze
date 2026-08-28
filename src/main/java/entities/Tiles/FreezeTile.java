package entities.Tiles;

import game.GameState;


public class FreezeTile implements Tile {

	@Override
	public void onEnter(GameState gameState) {
		gameState.freezePlayer(3000);
	}

	@Override
	public boolean isWalkable() {
		return true;
	}
}
