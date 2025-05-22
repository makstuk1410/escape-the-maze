package entities.Cells;

import managment.ScoreManager;

public class End extends EmptyWay {

    private Runnable onReached;

    public void setOnReached(Runnable action) {
        this.onReached = action;
    }

    @Override
    public void doEffects() {
        ScoreManager.addScore(100);
        if (onReached != null) {
            onReached.run();
        }
    }
}
