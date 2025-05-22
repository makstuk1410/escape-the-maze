package gui.mainScreens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.text.Text;

public abstract class BaseScreen extends VBox {

    public BaseScreen() {
        setSpacing(20);
        setPadding(new Insets(50));
        setAlignment(Pos.TOP_CENTER);

        // 🟡 Фон — золотистий
        setBackground(new Background(new BackgroundFill(
                Color.web("#D9A74A"), CornerRadii.EMPTY, Insets.EMPTY
        )));

        // Заголовок з обводкою
        Text title = new Text("ESCAPE THE MAZE");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 130));
        title.setFill(Color.WHITE); // основний колір тексту
        title.setStroke(Color.BLACK); // чорна обводка
        title.setStrokeWidth(2); // товщина обводки
        VBox.setMargin(title, new Insets(0, 0, 170, 0)); // відступ вниз

        getChildren().add(title);
        getChildren().add(createContent());
    }

    protected abstract Node createContent();
}
