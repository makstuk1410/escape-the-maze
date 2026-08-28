package algorithms;

import entities.MazeObjects.MazeGrid;

public final class MazeGeneratorFactory {

    private MazeGeneratorFactory() {
    }

    public static MazeGenerator create(
            Class<? extends MazeGenerator> generatorClass,
            MazeGrid maze
    ) {
        if (generatorClass == DFSMazeGenerator.class) {
            return new DFSMazeGenerator(maze);
        }
        if (generatorClass == PrimsMazeGenerator.class) {
            return new PrimsMazeGenerator(maze);
        }
        if (generatorClass == KruskalMazeGenerator.class) {
            return new KruskalMazeGenerator(maze);
        }
        if (generatorClass == BinaryTreeMazeGenerator.class) {
            return new BinaryTreeMazeGenerator(maze);
        }

        throw new IllegalArgumentException(
                "Unsupported maze generator: " + generatorClass.getName()
        );
    }
}
