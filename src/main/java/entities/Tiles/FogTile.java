package entities.Tiles;

import game.GameState;

public class FogTile implements Tile {

	@Override
	public void onEnter(GameState gameState) {
		gameState.activateFog(4000);
	}

	@Override
	public boolean isWalkable() {
		return true;
	}

    
}
