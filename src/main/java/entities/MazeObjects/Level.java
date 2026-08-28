
package entities.MazeObjects;

import algorithms.GeneratorType;


public class Level {
    private final String name;
    private final int width;
    private final int height;
    private final GeneratorType generatorType;
    private final int hardness;
    
    
    public Level(String name, int height, int width, GeneratorType generatorType, int hardness){
        this.name = name;
        this.height = height;
        this.width = width;
        this.generatorType = generatorType;
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
    
    public GeneratorType getGeneratorType(){
        return generatorType;
    }
    
    
}
