package entities;

import algorithms.DFSMazeGenerator;
import algorithms.MazeGenerator;
import java.util.Random;

public class Maze {

    private int[][] maze;
    private final int height;
    private final int width;
    private final int startX;
    private final int startY;
    private int endX;
    private int endY;

    
    //-----------------------------------------------------------GETTERS--------------------------------------------------
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

    
    
    
    

    public Maze(int height, int width) {
        this.height = 2 * height + 1;
        this.width = 2 * width + 1;
        this.maze = new int[this.height][this.width];
        Random r = new Random();
        startX = r.nextInt((this.width - 1) / 2) * 2 + 1;
        startY = 0;
        maze[startY][startX]= 0;
        maze[startY+1][startX]= 0;
    }

    public void create(){
        MazeGenerator mg = new DFSMazeGenerator(this);
        mg.createRandomMaze();
        chooseEndPoint();
        printMaze();
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
                    System.out.print("◻️");
                }

                if (maze[i][j] == 1) {
                    System.out.print("⬛️");
                }
            }
        }
        System.out.print("\n");
    }

}
