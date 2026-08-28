package entities.MazeObjects;

import algorithms.MazeGenerator;
import algorithms.MazeGeneratorFactory;
import entities.Tiles.EmptyTile;
import entities.Tiles.EndTile;
import entities.Tiles.FogTile;
import entities.Tiles.FreezeTile;
import entities.Tiles.GoldTile;
import entities.Tiles.SpikesTile;
import entities.Tiles.Tile;
import entities.Tiles.Wall;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class Maze {

    private final MazeGrid grid;
    private final Tile[][] tiles;
    private final int height;
    private final int width;
    private final int startX;
    private final int startY;
    private final Random random = new Random();
    private int endX;
    private int endY;
        private final List<Supplier<Tile>> obstacles = List.of(
            SpikesTile::new,
            FreezeTile::new,
            GoldTile::new,
            FogTile::new
        );
    

    //-----------------------------------------------------------GETTERS--------------------------------------------------
    public Tile[][] getTileMaze() {
        return tiles;

    }

    public Tile getTileValue(int y, int x) {
        return tiles[y][x];
    }

    public int getValue(int y, int x) {
        return grid.getValue(y, x);
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

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }
    //----------------------------------------------------------GETTERS-------------------------------------------------

    public Maze(int height, int width, Class<? extends MazeGenerator> generatorClass) {
        this.grid = new MazeGrid(height, width);
        this.height = grid.getHeight();
        this.width = grid.getWidth();
        this.startX = grid.getStartX();
        this.startY = grid.getStartY();

        MazeGeneratorFactory.create(generatorClass, grid).createRandomMaze();
        grid.setValue(startY, startX, 0);
        chooseEndPoint();

        tiles = new Tile[this.height][this.width];
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                tiles[i][j] = (grid.getValue(i, j) == 0) ? new EmptyTile() : new Wall();
            }
        }

        tiles[endY][endX] = new EndTile();

        for (int row = 1; row < height - 1; row++) {
            for (int col = (row % 2) + 1; col < width - 1; col += 2) {
                if (tiles[row][col] instanceof EmptyTile) {
                    if (random.nextDouble() < management.GameConfig.OBSTACLE_CHANCE) {
                        Supplier<Tile> obstacleSupplier = obstacles.get(random.nextInt(obstacles.size()));
                        tiles[row][col] = obstacleSupplier.get();
                    }
                }
            }
        }

    }

    private void chooseEndPoint() {
        int[] endXArray = new int[(width - 1) / 2];
        int endN = 0;
        for (int i = 1; i < width - 1; i += 2) {
            if (grid.getValue(height - 2, i) == 0) {
                endXArray[endN++] = i;
            }
        }

        if (endN == 0) {
            throw new IllegalStateException("Maze generation produced no exit");
        }

        endX = endXArray[random.nextInt(endN)];
        endY = height - 1;
        grid.setValue(endY, endX, 0);
    }

    public void printMaze() {
        for (int i = 0; i < height; i++) {
            System.out.print("\n");
            for (int j = 0; j < width; j++) {
                if (grid.getValue(i, j) == 0) {
                    System.out.print("  ️");
                }

                if (grid.getValue(i, j) == 1) {
                    System.out.print("█️█");
                }
            }
        }
        System.out.print("\n");
    }

}
