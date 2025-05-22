package managment;

import entities.MazeObjects.Level;
import entities.MazeObjects.Levels;
import entities.MazeObjects.Maze;
import entities.MazeObjects.Player;
import gui.Game.GameScreen;
import gui.mainScreens.ScreenManager;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class GameManager {
    
    public static int speed = 5;

    private static Maze maze = null;
    private static Player player = null;

    public static Maze getMaze() {
        return maze;
    }

    public static Player getPlayer() {
        return player;
    }

    public GameManager(Stage primaryStage) {
        ScreenManager.getInstance().setStage(primaryStage);
        ScreenManager.getInstance().switchScreen("menu");

        primaryStage.setTitle("Escape the Maze");

        primaryStage.setWidth(Screen.getPrimary().getBounds().getWidth());
        primaryStage.setHeight(Screen.getPrimary().getBounds().getHeight());
        primaryStage.setMaximized(true);

        primaryStage.show();
    }
    
    
    public static StackPane startGame() {
        Level level = Levels.getLevel(Levels.chosenLevel);
        maze = new Maze(level.getHeight(), level.getWidth(), level.getGeneratorClass());
        player = new Player(maze.getStartY(), maze.getStartX());
        
        return new GameScreen(player, maze);
    }
}
