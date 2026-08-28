package gui.mainScreens;

import gui.UIFactory;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

public class MenuScreen extends BaseScreen {

    
    
    @Override
    protected Node createContent() {
        VBox menuBox = new VBox(50);
        menuBox.setAlignment(Pos.CENTER);

        Button startBtn = UIFactory.createButton("START", 500, 120, 2, MAIN_BUTTON_COLOR, 60, 2.0);
        Button rankingBtn = UIFactory.createButton("RANKING", 500, 120, 2, MAIN_BUTTON_COLOR, 60, 2.0);
        Button exitBtn = UIFactory.createButton("EXIT", 500, 120, 2, MAIN_BUTTON_COLOR, 60, 2.0);

        
        exitBtn.setOnAction(e -> System.exit(0));
        startBtn.setOnAction(e -> ScreenManager.getInstance().switchScreen("difficulty"));
        rankingBtn.setOnAction(e -> ScreenManager.getInstance().switchScreen("ranking"));

        menuBox.getChildren().addAll(startBtn, rankingBtn, exitBtn);
        return menuBox;
    }

}
