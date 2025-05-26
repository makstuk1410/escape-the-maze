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

public class DifficultyScreen extends BaseScreen {

    private final String CARD_COLOR = "#00a5de";
    private final String CHOOSE_BUTTON_COLOR = "#0086b5";
    
    @Override
    protected Node createContent() {
        VBox root = new VBox(60);
        root.setAlignment(Pos.CENTER);

        HBox difficultyBox = new HBox(30);
        difficultyBox.setAlignment(Pos.CENTER);

        for (int i = 0; i < Levels.size(); i++) {
            Level level = Levels.getLevel(i);
            difficultyBox.getChildren().add(createDifficultyCard(i, level.getHeight() + "x" + level.getWidth(), "Difficulty level: " + level.getHardness()+ "/5"));
        }

        HBox bottomButtons = new HBox(40);
        bottomButtons.setAlignment(Pos.CENTER);

        Button menuBtn = Instruments.createButton("MENU", 300, 120, 2, MAIN_BUTTON_COLOR, 40, 1.5);
        menuBtn.setOnAction(e -> ScreenManager.getInstance().switchScreen("menu"));

        Button playBtn = Instruments.createButton("PLAY", 300, 120, 2, MAIN_BUTTON_COLOR, 40, 1.5);
        playBtn.setOnAction(e -> ScreenManager.getInstance().switchScreen("game"));

        bottomButtons.getChildren().addAll(menuBtn, playBtn);

        root.getChildren().addAll(difficultyBox, bottomButtons);
        return root;
    }

    private VBox createDifficultyCard(int level, String sizeText, String winText) {
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

        Text levelText = Instruments.createOutlinedText(Levels.getLevel(level).getName(), 50, 1.5);
        Text sizeTextNode = Instruments.createOutlinedText("Size: " + sizeText, 45, 1.5);
        Text winTextNode = Instruments.createOutlinedText(winText, 45, 1.5);

        Button chooseBtn = Instruments.createButton("CHOOSE", 150, 50, 2, CHOOSE_BUTTON_COLOR, 40, 1.5);
        chooseBtn.setOnAction(e -> {
            Levels.chosenLevel = level;
            System.out.println("Chosen: " + Levels.chosenLevel);
        });

        card.getChildren().addAll(levelText, sizeTextNode, winTextNode, chooseBtn);
        return card;
    }

}
