package com.makstuk.escapethemaze.backend.domain.maze;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MazeGridTest {

    @Test
    void convertsPathsAndWallsToTileTypes() {
        MazeGrid grid = new MazeGrid(1, 1);
        grid.setValue(0, 0, 1);
        grid.setValue(1, 1, 0);
        grid.setValue(2, 2, 1);

        TileType[][] tiles = grid.toTileTypes();

        assertThat(tiles).hasDimensions(3, 3);
        assertThat(tiles[0][0]).isEqualTo(TileType.WALL);
        assertThat(tiles[1][1]).isEqualTo(TileType.EMPTY);
        assertThat(tiles[2][2]).isEqualTo(TileType.WALL);
    }
}
