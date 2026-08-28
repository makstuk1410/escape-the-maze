package gui.mainScreens;

import javafx.scene.Parent;
import entities.MazeObjects.Level;
import management.GameManager;

public class ScreenFactory {
    public static Parent create(String screenName) {
        return switch (screenName) {
            case "menu" -> new MenuScreen();
            case "difficulty" -> new DifficultyScreen();
            case "ranking" -> new RankingScreen();
            case "game" -> {
                Level level = ScreenManager.getInstance().getSelectedLevel();
                if (level == null) {
                    throw new IllegalStateException("No level selected");
                }
                yield GameManager.startGame(level);
            }
            default -> throw new IllegalArgumentException("Unknown screen: " + screenName);
        };
    }
}
