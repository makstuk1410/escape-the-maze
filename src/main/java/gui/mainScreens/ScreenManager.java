package gui.mainScreens;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class ScreenManager {
    private static ScreenManager instance;
    private Stage stage;

    private ScreenManager() {}

    public static ScreenManager getInstance() {
        if (instance == null) {
            instance = new ScreenManager();
        }
        return instance;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void switchScreen(String screenName) {
        Scene scene = new Scene(ScreenFactory.create(screenName));
        stage.setScene(scene);
    }
}
