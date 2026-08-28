package management;

public class GameConfig {
    public static final int TILE_SIZE = 80;
    public static final int VIEW_SIZE = 11;
    public static final double JUMP_MAX_SCALE = 1.5;
    public static final long JUMP_DURATION_MS = 650;
    public static final long FOG_DURATION_MS = 4000;
    public static final long FREEZE_DURATION_MS = 3000;
    public static final int SPIKES_DAMAGE = 20;
    public static final int GOLD_SCORE = 10;
    public static final int END_SCORE = 100;
    public static final double OBSTACLE_CHANCE = 0.08;
    public static final int PLAYER_SIZE = TILE_SIZE - 20;
    public static final int PLAYER_SPAWN_OFFSET = 10;
    public static final int HEALTH_BAR_WIDTH = 20;
    public static final int HEALTH_BAR_HEIGHT = 200;
    public static final int HUD_MARGIN = 50;
    public static final int TOP_BAR_PADDING = 10;
    public static final int POPUP_WIDTH = 300;
    public static final int POPUP_HEIGHT = 200;
    public static final double OVERLAY_OPACITY = 0.5;
    public static final int SCORE_FONT_SIZE = 51;
    public static final double SCORE_BORDER_SIZE = 2;
    public static final int TIMER_FONT_SIZE = 40;
    public static final double TIMER_BORDER_SIZE = 1;


    private GameConfig() {
    }
}
