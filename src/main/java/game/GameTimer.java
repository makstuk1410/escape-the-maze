package game;

public class GameTimer {

    private int timeLeft;
    private Runnable onTimeEnd;
    private double accumulator;

    public boolean update(double deltaTime) {
        if (deltaTime < 0 || timeLeft == 0) {
            return false;
        }

        accumulator += deltaTime;
        boolean ticked = false;

        if (accumulator >= 1.0) {
            int ticks = (int) accumulator;
            accumulator -= ticks;
            for (int i = 0; i < ticks && timeLeft > 0; i++) {
                tick();
                ticked = true;
            }
        }

        return ticked;
    }

    public GameTimer(int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Timer duration cannot be negative");
        }

        this.timeLeft = seconds;
    }

    public void tick() {
        if (timeLeft <= 0) {
            return;
        }

        timeLeft--;

        if (timeLeft == 0 && onTimeEnd != null) {
            onTimeEnd.run();
        }
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void reset(int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Timer duration cannot be negative");
        }

        this.timeLeft = seconds;
        this.accumulator = 0;
    }

    public void setOnTimeEnd(Runnable action) {
        this.onTimeEnd = action;
    }

    public String getFormattedTime() {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;

        return String.format(
                "Time: %02d:%02d",
                minutes,
                seconds);
    }
}