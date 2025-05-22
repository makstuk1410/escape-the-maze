package gui.Game;

import gui.mainScreens.ScreenManager;
import entities.MazeObjects.Levels;
import gui.Instruments;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import managment.ScoreManager;

public class ResultWindow extends VBox {

    public ResultWindow(String message, int score) {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(20));

        setMinWidth(600); // <--- розмір
        setMinHeight(400);
        setBackground(new Background(new BackgroundFill(Color.rgb(245, 175, 132), new CornerRadii(10), Insets.EMPTY)));
        setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(10), BorderWidths.DEFAULT)));

        Text title = Instruments.createOutlinedText(message, 100, 1);
        Text scoreLabel = Instruments.createOutlinedText("Your score: " + ScoreManager.getCurrentScore(), 50, 1);
        ScoreManager.updateScore(Levels.getLevel(Levels.chosenLevel).getName(), ScoreManager.getCurrentScore());
        ScoreManager.setCurrentScore(0);
        HBox buttons = new HBox(20);
        buttons.setAlignment(Pos.CENTER);

        Button menuButton = Instruments.createButton("MENU", 100, 70, 1, "#edac42", 30, 1);
        menuButton.setOnAction(e -> ScreenManager.getInstance().switchScreen("menu"));

        Button tryAgainButton = Instruments.createButton("TRY AGAIN", 100, 70, 1, "#edac42", 30, 1);
        tryAgainButton.setOnAction(e -> ScreenManager.getInstance().switchScreen("game"));

        buttons.getChildren().addAll(menuButton, tryAgainButton);
        getChildren().addAll(title, scoreLabel, buttons);
    }
}
