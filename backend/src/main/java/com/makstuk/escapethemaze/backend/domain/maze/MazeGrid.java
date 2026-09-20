package com.makstuk.escapethemaze.backend.domain.maze;

import java.util.Random;
import java.util.Objects;

/**
 * Numeric maze grid used while a generator carves walls (1) and paths (0).
 */
public class MazeGrid {

    private final int[][] cells;
    private final int height;
    private final int width;
    private final int startX;
    private final int startY = 0;

    public MazeGrid(int height, int width) {
        this(height, width, new Random());
    }

    public MazeGrid(int height, int width, Random random) {
        if (height < 1 || width < 1) {
            throw new IllegalArgumentException("Maze dimensions must be positive");
        }
        Objects.requireNonNull(random, "random must not be null");

        this.height = 2 * height + 1;
        this.width = 2 * width + 1;
        this.cells = new int[this.height][this.width];
        this.startX = random.nextInt((this.width - 1) / 2) * 2 + 1;
        cells[startY + 1][startX] = 0;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getValue(int y, int x) {
        return cells[y][x];
    }

    public void setValue(int y, int x, int value) {
        cells[y][x] = value;
    }

    /**
     * Converts generator values into the gameplay tile representation.
     */
    public TileType[][] toTileTypes() {
        TileType[][] tiles = new TileType[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = cells[y][x] == 0 ? TileType.EMPTY : TileType.WALL;
            }
        }
        return tiles;
    }
}
