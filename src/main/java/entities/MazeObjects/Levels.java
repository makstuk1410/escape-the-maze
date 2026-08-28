package entities.MazeObjects;

import algorithms.GeneratorType;
import java.util.ArrayList;

public class Levels {

    private static final ArrayList<Level> levels = new ArrayList<>();

    static {
        levels.add(new Level("EASY", 15, 15, GeneratorType.DFS, 1));
        levels.add(new Level("NORMAL", 20, 20, GeneratorType.PRIM, 3));
        levels.add(new Level("HARD", 20, 20, GeneratorType.KRUSKAL, 4));
        levels.add(new Level("INSANE", 30, 30, GeneratorType.KRUSKAL, 5));
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
