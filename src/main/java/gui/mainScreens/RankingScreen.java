package gui.mainScreens;

import entities.MazeObjects.Level;
import entities.MazeObjects.Levels;
import gui.Instruments;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import managment.ScoreManager;

public class RankingScreen extends BaseScreen {

    private final String CARD_COLOR = "#00a5de";
    
    @Override
    protected Node createContent() {
        VBox root = new VBox(60);
        root.setAlignment(Pos.CENTER);

        HBox difficultyBox = new HBox(30);
        difficultyBox.setAlignment(Pos.CENTER);
        
        for(int i = 0; i < Levels.size(); i++){
            Level level = Levels.getLevel(i);
            difficultyBox.getChildren().add(createDifficultyCard(level.getName(), ScoreManager.getScore(level.getName())));
        }

        // Нижні кнопки
        HBox bottomButtons = new HBox(40);
        bottomButtons.setAlignment(Pos.CENTER);

        Button menuBtn = Instruments.createButton("MENU", 300, 120, 2, MAIN_BUTTON_COLOR, 40, 1.5);
        menuBtn.setOnAction(e -> ScreenManager.getInstance().switchScreen("menu"));

        bottomButtons.getChildren().addAll(menuBtn);

        root.getChildren().addAll(difficultyBox, bottomButtons);
        return root;
    }

    private VBox createDifficultyCard(String level, int score) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(350);
        card.setPrefHeight(490);
        card.setStyle(
                "-fx-background-color: " + CARD_COLOR + ";"
                + "-fx-border-color: black;"
                + "-fx-border-width: 2;"
                + "-fx-border-radius: 10;"
                + "-fx-background-radius: 10;"
        );

        Text levelText = Instruments.createOutlinedText(level, 60, 1.5);
        Text sizeTextNode = Instruments.createOutlinedText("High score:", 40, 1.5);
        Text winTextNode = Instruments.createOutlinedText(""+score, 45, 1.5);

        card.getChildren().addAll(levelText, sizeTextNode, winTextNode);
        return card;
    }
}
