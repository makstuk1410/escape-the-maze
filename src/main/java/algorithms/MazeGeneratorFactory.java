package algorithms;

import entities.MazeObjects.MazeGrid;

public final class MazeGeneratorFactory {

    private MazeGeneratorFactory() {
    }

    public static MazeGenerator create(
            GeneratorType generatorType,
            MazeGrid maze
    ) {
        return switch (generatorType) {
            case DFS -> new DFSMazeGenerator(maze);
            case PRIM -> new PrimsMazeGenerator(maze);
            case KRUSKAL -> new KruskalMazeGenerator(maze);
            case BINARY_TREE -> new BinaryTreeMazeGenerator(maze);
        };
    }
}
