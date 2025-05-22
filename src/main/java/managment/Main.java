package managment;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        GameManager gameManager = new GameManager(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
//TODO: refaktoryzować tak by lepiej tworzyć przyciski


//będzie klasa LEvel z 