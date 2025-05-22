package gui.mainScreens;

import gui.Instruments;
import gui.mainScreens.ScreenManager;
import gui.mainScreens.BaseScreen;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

public class MenuScreen extends BaseScreen {

    @Override
    protected Node createContent() {
        VBox menuBox = new VBox(50);
        menuBox.setAlignment(Pos.CENTER);

        Button startBtn = Instruments.createButton("START", 500, 120, 2, "#C18C72", 60, 2.0);
        Button rankingBtn = Instruments.createButton("RANKING", 500, 120, 2, "#C18C72", 60, 2.0);
        Button exitBtn = Instruments.createButton("EXIT", 500, 120, 2, "#C18C72", 60, 2.0);

        
        exitBtn.setOnAction(e -> System.exit(0)); // вихід
        startBtn.setOnAction(e -> ScreenManager.getInstance().switchScreen("difficulty")); // вихід
        rankingBtn.setOnAction(e -> ScreenManager.getInstance().switchScreen("ranking")); // вихід

        menuBox.getChildren().addAll(startBtn, rankingBtn, exitBtn);
        return menuBox;
    }

}
