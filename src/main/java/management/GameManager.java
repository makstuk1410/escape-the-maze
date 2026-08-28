package management;

import entities.MazeObjects.Level;
import entities.MazeObjects.Levels;
import entities.MazeObjects.Maze;
import entities.MazeObjects.Player;
import game.GameTimer;
import gui.game.GameScreen;
import gui.mainScreens.ScreenManager;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import game.GameState;

public class GameManager {
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
        Maze maze = new Maze(level.getHeight(), level.getWidth(), level.getGeneratorClass());
        Player player = new Player(maze.getStartY(), maze.getStartX());        
        GameTimer timer = new GameTimer(300);
        GameState gameState = new GameState(player);
        return new GameScreen(gameState, maze, timer);
    }
}
