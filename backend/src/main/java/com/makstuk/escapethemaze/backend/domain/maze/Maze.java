package com.makstuk.escapethemaze.backend.domain.maze;

import java.util.Objects;

/**
 * The logical tile board generated for one game session.
 */
public class Maze {

    private final int width;
    private final int height;
    private final TileType[][] tiles;
    private final Position start;
    private final Position exit;
    private final GeneratorType generatorType;

    public Maze(TileType[][] tiles, Position start, Position exit, GeneratorType generatorType) {
        this.tiles = copyAndValidateTiles(tiles);
        this.height = tiles.length;
        this.width = tiles[0].length;
        this.start = Objects.requireNonNull(start, "start must not be null");
        this.exit = Objects.requireNonNull(exit, "exit must not be null");
        this.generatorType = Objects.requireNonNull(generatorType, "generatorType must not be null");

        if (!isInBounds(start.x(), start.y()) || !isInBounds(exit.x(), exit.y())) {
            throw new IllegalArgumentException("Start and exit must be inside the maze");
        }
        if (!tileAt(start.x(), start.y()).isWalkable()) {
            throw new IllegalArgumentException("Start tile must be walkable");
        }
        if (tileAt(exit.x(), exit.y()) != TileType.EXIT) {
            throw new IllegalArgumentException("Exit position must contain an EXIT tile");
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Position getStart() {
        return start;
    }

    public Position getExit() {
        return exit;
    }

    public GeneratorType getGeneratorType() {
        return generatorType;
    }

    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public TileType tileAt(int x, int y) {
        if (!isInBounds(x, y)) {
            throw new IllegalArgumentException("Position is outside the maze");
        }
        return tiles[y][x];
    }

    public void replaceTile(int x, int y, TileType tileType) {
        if (!isInBounds(x, y)) {
            throw new IllegalArgumentException("Position is outside the maze");
        }
        tiles[y][x] = Objects.requireNonNull(tileType, "tileType must not be null");
    }

    public TileType[][] getTiles() {
        TileType[][] copy = new TileType[height][width];
        for (int y = 0; y < height; y++) {
            System.arraycopy(tiles[y], 0, copy[y], 0, width);
        }
        return copy;
    }

    private static TileType[][] copyAndValidateTiles(TileType[][] source) {
        if (source == null || source.length == 0 || source[0] == null || source[0].length == 0) {
            throw new IllegalArgumentException("Maze tiles must be a non-empty rectangle");
        }

        int width = source[0].length;
        TileType[][] copy = new TileType[source.length][width];
        for (int y = 0; y < source.length; y++) {
            if (source[y] == null || source[y].length != width) {
                throw new IllegalArgumentException("Maze tiles must be rectangular");
            }
            for (int x = 0; x < width; x++) {
                copy[y][x] = Objects.requireNonNull(source[y][x], "Maze tiles must not contain null");
            }
        }
        return copy;
    }
}
