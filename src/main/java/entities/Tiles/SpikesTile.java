package entities.Tiles;

import game.GameState;


public class SpikesTile implements Tile {

	@Override
	public void onEnter(GameState gameState) {
        gameState.applyDamage(20);
	}

	@Override
	public boolean isWalkable() {
		return true;
	}


}
