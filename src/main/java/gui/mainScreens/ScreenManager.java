package gui.mainScreens;

import javafx.scene.Scene;
import javafx.stage.Stage;
import entities.MazeObjects.Level;

public class ScreenManager {

    private static ScreenManager instance;
    private Stage stage;
    private Level selectedLevel;

    private ScreenManager() {
    }

    public static ScreenManager getInstance() {
        if (instance == null) {
            instance = new ScreenManager();
        }
        return instance;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setSelectedLevel(Level selectedLevel) {
        this.selectedLevel = selectedLevel;
    }

    public Level getSelectedLevel() {
        return selectedLevel;
    }

    public void switchScreen(String screenName) {
        Scene scene = new Scene(ScreenFactory.create(screenName));
        stage.setScene(scene);
        javafx.application.Platform.runLater(() -> {
            stage.sizeToScene();
            stage.centerOnScreen();
        });
    }
}
