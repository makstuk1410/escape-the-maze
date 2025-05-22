package entities.MazeObjects;

import gui.Instruments;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Timer {

    private int timeLeft;
    private final Text text;
    private final Timeline timeline;
    private Runnable onTimeEnd;

    public Timer(int seconds) {
        this.timeLeft = seconds;
        this.text = Instruments.createOutlinedText(formatTime(timeLeft), 40, 1);

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void tick() {
        timeLeft--;
        text.setText(formatTime(timeLeft));

        if (timeLeft <= 0) {
            stop();
            if (onTimeEnd != null) {
                onTimeEnd.run();
            }
        }
    }

    public void start() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }

    public void reset(int seconds) {
        stop();
        this.timeLeft = seconds;
        text.setText(formatTime(timeLeft));
    }

    public Text getLabel() {
        return text;
    }

    public void setOnTimeEnd(Runnable action) {
        this.onTimeEnd = action;
    }

    private String formatTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("Time: %02d:%02d", min, sec);
    }
}
