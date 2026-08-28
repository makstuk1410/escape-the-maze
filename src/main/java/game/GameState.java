package game;

import entities.MazeObjects.Player;

public class GameState {

    private final Player player;
    private int score = 0;
    private boolean gameWon = false;
    private long freezeUntil = 0;
    private long fogUntil = 0;
    private long damageCooldownUntil = 0;

    public boolean canTakeDamage() {
        return System.currentTimeMillis() >= damageCooldownUntil;
    }

    public void applyDamage(int damage) {
        if (!canTakeDamage()) {
            return;
        }

        player.takeDamage(damage);
        damageCooldownUntil = System.currentTimeMillis() + 1000;
    }

    public void freezePlayer(long durationMillis) {
        this.freezeUntil = System.currentTimeMillis() + durationMillis;
    }

    public boolean isPlayerFrozen() {
        return System.currentTimeMillis() < freezeUntil;
    }

    public int getPlayerSpeed() {
        if (isPlayerFrozen()) {
            return 2;
        }

        return player.getSpeed();
    }

    public GameState(Player player) {
        this.player = player;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public void win() {
        gameWon = true;
    }

    public void activateFog(long durationMillis) {
        this.fogUntil = System.currentTimeMillis() + durationMillis;
    }

    public boolean isFogActive() {
        return System.currentTimeMillis() < fogUntil;
    }

    public Player getPlayer() {
        return player;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int amount) {
        score += amount;
    }
}