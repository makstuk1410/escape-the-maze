
package gui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;


public class Instruments {
    private final static String TEXT_COLOR = "#00334f";
    private final static String TEXT_BORDER_COLOR = "#EEEEEE";

    
    public static Text createOutlinedText(String content, int fontSize, double borderSize) {
        Text text = new Text(content);
        text.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
        text.setFill(Color.web(TEXT_COLOR));
        text.setStroke(Color.web(TEXT_BORDER_COLOR));
        text.setStrokeWidth(borderSize);
        return text;
    }
    
   public static Button createButton(String text, int width, int height, double border, String color, int fontSize, double fontBorder) {

        Text styledText = createOutlinedText(text, fontSize, fontBorder);
        styledText.setTranslateY(3); 
        
        StackPane centeredGraphic = new StackPane(styledText);
        centeredGraphic.setPrefSize(width, height); // точно такий самий як кнопка
        centeredGraphic.setAlignment(Pos.CENTER); // ❗ по центру

        Button btn = new Button();
        btn.setPrefWidth(width);
        btn.setPrefHeight(height);
        btn.setText(""); // вимикаємо стандартний текст
        btn.setGraphic(centeredGraphic); // вставляємо графіку
        btn.setStyle(
                "-fx-background-color: "+color + ";"
                + "-fx-background-radius: 30;"
                + "-fx-border-color: black;"
                + "-fx-border-width: "+border+";"
                + "-fx-border-radius: 30;"
        );
        String defaultStyle = btn.getStyle();

        
        
        String colorRegex = "-fx-background-color:\\s*#([0-9a-fA-F]{6});";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(colorRegex).matcher(defaultStyle);

        if (matcher.find()) {
            String originalHex = matcher.group(1);
            String lighterHex = lightenColor(originalHex, 0.1); // 10% світліше

            String hoverStyle = defaultStyle.replace(
                    "#" + originalHex,
                    "#" + lighterHex
            );

            btn.setCursor(javafx.scene.Cursor.HAND);
            btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
            btn.setOnMouseExited(e -> btn.setStyle(defaultStyle));
        }
        return btn;
    }

    protected static String lightenColor(String hexColor, double factor) {
        int r = Integer.parseInt(hexColor.substring(0, 2), 16);
        int g = Integer.parseInt(hexColor.substring(2, 4), 16);
        int b = Integer.parseInt(hexColor.substring(4, 6), 16);

        r = (int) Math.min(255, r + (255 - r) * factor);
        g = (int) Math.min(255, g + (255 - g) * factor);
        b = (int) Math.min(255, b + (255 - b) * factor);

        return String.format("%02X%02X%02X", r, g, b);
    }
}
