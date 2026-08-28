package entities.MazeObjects;

import java.util.Random;

public class MazeGrid {

    private final int[][] cells;
    private final int height;
    private final int width;
    private final int startX;
    private final int startY = 0;

    public MazeGrid(int height, int width) {
        if (height < 1 || width < 1) {
            throw new IllegalArgumentException("Maze dimensions must be positive");
        }

        this.height = 2 * height + 1;
        this.width = 2 * width + 1;
        this.cells = new int[this.height][this.width];
        this.startX = new Random().nextInt((this.width - 1) / 2) * 2 + 1;
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

}
