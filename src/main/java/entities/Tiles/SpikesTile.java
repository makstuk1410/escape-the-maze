package entities.Tiles;

import game.GameState;
import management.GameConfig;


public class SpikesTile implements Tile {

	@Override
	public void onEnter(GameState gameState) {
		gameState.applyDamage(GameConfig.SPIKES_DAMAGE);
	}

	@Override
	public boolean isWalkable() {
		return true;
	}


}
