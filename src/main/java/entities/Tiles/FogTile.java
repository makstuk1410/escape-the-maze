package entities.Tiles;

import game.GameState;
import management.GameConfig;

public class FogTile implements Tile {

	@Override
	public void onEnter(GameState gameState) {
		gameState.activateFog(GameConfig.FOG_DURATION_MS);
	}

	@Override
	public boolean isWalkable() {
		return true;
	}

    
}
