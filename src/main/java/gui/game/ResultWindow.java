package gui.game;


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

public class ResultWindow extends VBox {

    private static final String BACKGROUND_COLOR = "#90caf9";
    private static final String MAIN_BUTTON_COLOR = "#00a5de";

    public ResultWindow(
            String message,
            int score,
            Runnable onMenu,
            Runnable onTryAgain
    ) {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(20));

        setMinWidth(600);
        setMinHeight(400);

        setBackground(new Background(
                new BackgroundFill(
                        Color.web(BACKGROUND_COLOR),
                        new CornerRadii(10),
                        Insets.EMPTY
                )
        ));

        setBorder(new Border(
                new BorderStroke(
                        Color.BLACK,
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(10),
                        BorderWidths.DEFAULT
                )
        ));

        Text title = Instruments.createOutlinedText(
                message,
                100,
                1
        );

        Text scoreLabel = Instruments.createOutlinedText(
                "Your score: " + score,
                50,
                1
        );

        HBox buttons = new HBox(20);
        buttons.setAlignment(Pos.CENTER);

        Button menuButton = Instruments.createButton(
                "MENU",
                100,
                70,
                1,
                MAIN_BUTTON_COLOR,
                30,
                1
        );

        Button tryAgainButton = Instruments.createButton(
                "TRY AGAIN",
                100,
                70,
                1,
                MAIN_BUTTON_COLOR,
                30,
                1
        );

        menuButton.setOnAction(e -> onMenu.run());
        tryAgainButton.setOnAction(e -> onTryAgain.run());

        buttons.getChildren().addAll(
                menuButton,
                tryAgainButton
        );

        getChildren().addAll(
                title,
                scoreLabel,
                buttons
        );
    }
}