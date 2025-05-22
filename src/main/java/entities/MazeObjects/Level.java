
package entities.MazeObjects;

import algorithms.MazeGenerator;


public class Level {
    private final String name;
    private final int width;
    private final int height;
    private final Class<? extends MazeGenerator> generatorClass;
    private final int hardness;
    
    
    public Level(String name, int height, int width, Class<? extends MazeGenerator> generatorClass, int hardness){
        this.name = name;
        this.height = height;
        this.width = width;
        this.generatorClass = generatorClass;
        this.hardness = hardness;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the ways
     */
    public int getHardness() {
        return hardness;
    }
    
    public Class<? extends MazeGenerator> getGeneratorClass(){
        return generatorClass;
    }
    
    
}
