package gui.mainScreens;

import gui.mainScreens.DifficultyScreen;
import javafx.scene.Parent;
import managment.GameManager;

public class ScreenFactory {
    public static Parent create(String screenName) {
        return switch (screenName) {
            case "menu" -> new MenuScreen();
            case "difficulty" -> new DifficultyScreen();
            case "ranking" -> new RankingScreen();
            case "game" -> GameManager.startGame();
            default -> throw new IllegalArgumentException("Unknown screen: " + screenName);
        };
    }
}
