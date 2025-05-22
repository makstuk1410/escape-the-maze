package entities.MazeObjects;

import algorithms.DFSMazeGenerator;
import algorithms.KruskalMazeGenerator;
import algorithms.PrimsMazeGenerator;
import java.util.ArrayList;

public class Levels {

    public static int chosenLevel = -1;

    private static final ArrayList<Level> levels = new ArrayList<>();

    static {
        levels.add(new Level("EASY", 15, 15, DFSMazeGenerator.class, 1));
        levels.add(new Level("NORMAL", 20, 20, PrimsMazeGenerator.class, 3));
        levels.add(new Level("HARD", 20, 20, KruskalMazeGenerator.class, 4));
        levels.add(new Level("INSANE", 30, 30, KruskalMazeGenerator.class, 5));
    }

    public static Level getLevel(int i) {
        if (i == -1) {
            throw new IllegalArgumentException("Choose a level");
        }

        return levels.get(i);
    }

    public static int size() {
        return levels.size();
    }
}
