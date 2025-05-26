package entities.MazeObjects;

import entities.Cells.Wall;
import entities.Cells.Freeze;
import entities.Cells.End;
import entities.Cells.EmptyWay;
import entities.Cells.Cell;
import entities.Cells.Gold;
import entities.Cells.Spikes;
import algorithms.MazeGenerator;
import entities.Cells.Fog;
import java.util.List;
import java.util.Random;

public class Maze {

    private int[][] maze;
    private Cell[][] cellMaze;
    private final int height;
    private final int width;
    private final int startX;
    private final int startY;
    private int endX;
    private int endY;
    private List<Class<? extends Cell>> obstacles = List.of(
        Spikes.class,
        Freeze.class,
        Gold.class,
        Fog.class
                );
    

    //-----------------------------------------------------------GETTERS--------------------------------------------------
    public int[][] getMaze() {
        return maze;
    }

    public Cell[][] getCellMaze() {
        return cellMaze;

    }

    public Cell getCellValue(int y, int x) {
        return cellMaze[y][x];
    }

    public int getValue(int y, int x) {
        return maze[y][x];
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

    //--------------------------------------------SETTERS-------------------------------------------------------
    public void setValue(int y, int x, int value) {
        this.maze[y][x] = value;
    }

    public void setEndX(int endX) {
        this.endX = endX;
    }

    public void setEndY(int endY) {
        this.endY = endY;
    }
    //--------------------------------------------SETTERS-------------------------------------------------------

    private Maze(int height, int width) {
        this.height = 2 * height + 1;
        this.width = 2 * width + 1;
        this.maze = new int[this.height][this.width];

        Random r = new Random();
        startX = r.nextInt((this.width - 1) / 2) * 2 + 1;
        startY = 0;

        maze[startY + 1][startX] = 0;

    }

    public Maze(int height, int width, Class<? extends MazeGenerator> generatorClass) {
        Maze newMaze = new Maze(height, width);

        MazeGenerator mg;
        try {
            mg = generatorClass
                    .getDeclaredConstructor(Maze.class)
                    .newInstance(newMaze);
        } catch (Exception e) {
            throw new RuntimeException("Nie udało sięstworzyć MazeGenerator: " + generatorClass.getSimpleName(), e);
        }

        mg.createRandomMaze();

        this.maze = newMaze.getMaze();
        this.height = 2 * height + 1;
        this.width = 2 * width + 1;

        startX = newMaze.getStartX();
        startY = newMaze.getStartY();
        maze[startY][startX] = 0;
        chooseEndPoint();

        cellMaze = new Cell[this.height][this.width];
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                cellMaze[i][j] = (maze[i][j] == 0) ? new EmptyWay() : new Wall();
            }
        }

        cellMaze[endY][endX] = new End();

        Random random = new Random();

        for (int row = 1; row < maze.length-1; row++) {
            for (int col = (row%2)+1; col < maze[0].length-1; col += 2) {
                if (cellMaze[row][col] instanceof EmptyWay) {
                    if (random.nextDouble() < 0.08) { // 10% шанс поставити перешкоду
                        
                        // Обираємо випадковий тип перешкоди
                        Class<? extends Cell> clazz = obstacles.get(random.nextInt(obstacles.size()));

                        try {
                            Cell obstacle = clazz.getDeclaredConstructor().newInstance(); // створюємо екземпляр
                            cellMaze[row][col] = obstacle;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    private void chooseEndPoint() {
        int[] endXArray = new int[(width - 1) / 2];
        int endN = 0;
        for (int i = 1; i < width - 1; i += 2) {
            if (maze[height - 2][i] == 0) {
                endXArray[endN++] = i;
            }
        }

        endX = endXArray[(new Random()).nextInt(endN)];
        endY = height - 1;
        maze[endY][endX] = 0;
    }

    public void printMaze() {
        for (int i = 0; i < height; i++) {
            System.out.print("\n");
            for (int j = 0; j < width; j++) {
                if (maze[i][j] == 0) {
                    System.out.print("  ️");
                }

                if (maze[i][j] == 1) {
                    System.out.print("█️█");
                }
            }
        }
        System.out.print("\n");
    }

}
