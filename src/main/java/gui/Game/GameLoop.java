package gui.Game;

import java.util.function.Consumer;

import javafx.animation.AnimationTimer;

public class GameLoop {

    private final Consumer<Double> onUpdate;
    private final AnimationTimer animationTimer;

    private long lastTime = 0;

    public GameLoop(Consumer<Double> onUpdate) {
        this.onUpdate = onUpdate;

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {

                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                double deltaTime = (now - lastTime) / 1_000_000_000.0;

                lastTime = now;

                onUpdate.accept(deltaTime);
            }
        };
    }

    public void start() {
        animationTimer.start();
    }

    public void stop() {
        animationTimer.stop();
        lastTime = 0;
    }
}