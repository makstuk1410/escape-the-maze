package gui.mainScreens;

import gui.Instruments;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.text.Text;

public abstract class BaseScreen extends VBox {

    private String BACKGROUND_COLOR = "#90caf9";
    protected final String MAIN_BUTTON_COLOR = "#00a5de";

    public BaseScreen() {
        setSpacing(20);
        setPadding(new Insets(50));
        setAlignment(Pos.TOP_CENTER);

        setBackground(new Background(new BackgroundFill(
                Color.web(BACKGROUND_COLOR), CornerRadii.EMPTY, Insets.EMPTY
        )));

        
        Text title = Instruments.createOutlinedText("ESCAPE THE MAZE", 130, 2);
        VBox.setMargin(title, new Insets(0, 0, 170, 0)); 

        getChildren().add(title);
        getChildren().add(createContent());
    }

    protected abstract Node createContent();
}
