package gui.Game;

import entities.MazeObjects.Timer;
import entities.MazeObjects.Player;
import entities.MazeObjects.Maze;
import entities.Cells.End;
import entities.Cells.Cell;
import gui.Instruments;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import managment.GameManager;

import java.util.HashSet;
import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.CacheHint;
import javafx.scene.text.Text;
import javafx.util.Duration;
import managment.ScoreManager;

public class GameScreen extends StackPane {

    public static final int TILE_SIZE = 80;
    private static final Canvas canvas = new Canvas(11 * TILE_SIZE, 11 * TILE_SIZE);
    private static final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    private static final Text score = Instruments.createOutlinedText("Score: " + ScoreManager.getCurrentScore(), 51, 2);
    private final Cell[][] maze;
    private final Player player;
    private Timer timer;
    private final HealthBar healthBar = new HealthBar();

    private boolean isPopupActive = false;
    private double cameraOffsetX = 0;
    private double cameraOffsetY = 0;

    private final GameRenderer renderer = new GameRenderer();
    private final MovementController movementController = new MovementController();
    private EffectProcessor effectProcessor;

    private static final Canvas fogCanvas = new Canvas(11 * TILE_SIZE, 11 * TILE_SIZE); // <- окремий шар
    private static final GraphicsContext fogGC = fogCanvas.getGraphicsContext2D();

    public GameScreen(Player player, Maze mazeObj) {
        this.maze = mazeObj.getCellMaze();
        this.player = player;

        initializePlayerPosition(mazeObj);
        setupCanvasEvents();
        setupLayout();
        setupTopBar();
        setupEffectProcessor();
        setupAnimation();
        setEndCallback(mazeObj);

    }

    public static void updateScore() {
        score.setText("Score: " + ScoreManager.getCurrentScore());
    }

    private void initializePlayerPosition(Maze mazeObj) {
        player.setPositionX(mazeObj.getStartX() * TILE_SIZE + 10);
        player.setPositionY(mazeObj.getStartY() * TILE_SIZE + 10);
    }

    private void setupCanvasEvents() {
        canvas.setFocusTraversable(true);
        canvas.requestFocus();

        canvas.setOnKeyPressed(e -> {
            if (!isPopupActive) {
                pressedKeys.add(e.getCode());
            }
            if (e.getCode() == KeyCode.SPACE && !player.isJumping()) {
                startJumpAnimation();
            }

        });

        canvas.setOnKeyReleased(e -> {
            if (!isPopupActive) {
                pressedKeys.remove(e.getCode());
            }
        });
    }

    private void startJumpAnimation() {
        player.setJumping(true);

        final double maxScale = 1.5;
        final double durationMs = 650;

        javafx.beans.property.DoubleProperty scaleProp = new javafx.beans.property.SimpleDoubleProperty(1.0);
        scaleProp.addListener((obs, oldVal, newVal) -> player.setScale(newVal.doubleValue()));

        Timeline jumpTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(scaleProp, 1.0)),
                new KeyFrame(Duration.millis(durationMs / 2), new KeyValue(scaleProp, maxScale)),
                new KeyFrame(Duration.millis(durationMs), new KeyValue(scaleProp, 1.0))
        );

        jumpTimeline.setOnFinished(e -> {
            player.setScale(1.0);
            player.setJumping(false);
        });

        jumpTimeline.play();
    }

    private static boolean setFog = false;

    private static void drawCanvasBorders(double scale) {
        double thickness = 3 * scale;
        double width = fogCanvas.getWidth();
        double height = fogCanvas.getHeight();

        fogGC.clearRect(0, 0, width, height); 

        fogGC.setFill(Color.web("#666d6f")); 

        fogGC.fillRect(0, 0, width, thickness);
        fogGC.fillRect(0, height - thickness, width, thickness);
        fogGC.fillRect(0, 0, thickness, height);
        fogGC.fillRect(width - thickness, 0, thickness, height);
    }

    public static void startFogAnimation() {
        if (setFog) {
            return;
        }
        setFog = true;
        final double maxScale = 80;
        final double durationMs = 4000;

        javafx.beans.property.DoubleProperty scaleProp = new javafx.beans.property.SimpleDoubleProperty(1.0);

        final long[] lastUpdateTime = {0};
        scaleProp.addListener((obs, oldVal, newVal) -> {
            long now = System.currentTimeMillis();
            if (now - lastUpdateTime[0] > 16) {
                drawCanvasBorders(newVal.doubleValue());
                lastUpdateTime[0] = now;
            }
        });
        
        Timeline fogTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(scaleProp, 1.0)),
                new KeyFrame(Duration.millis(durationMs / 4), new KeyValue(scaleProp, maxScale)),
                new KeyFrame(Duration.millis(durationMs / 4 * 3), new KeyValue(scaleProp, maxScale)),
                new KeyFrame(Duration.millis(durationMs), new KeyValue(scaleProp, 1.0))
        );

        fogTimeline.setOnFinished(e -> {
            drawCanvasBorders(0.0);
            setFog = false;
        });

        fogTimeline.play();
    }

    private void setupLayout() {
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: black;");
        getChildren().add(canvas);
        getChildren().add(fogCanvas);

        fogCanvas.setCache(true);
        fogCanvas.setCacheHint(CacheHint.SPEED);

        healthBar.setPrefSize(20, 200);
        healthBar.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(healthBar, Pos.TOP_RIGHT);
        StackPane.setMargin(healthBar, new Insets(50));
        getChildren().add(healthBar);

        StackPane.setAlignment(score, Pos.TOP_LEFT);

        StackPane.setMargin(score, new Insets(50));
        getChildren().add(score);
    }

    private void setupTopBar() {
        timer = new Timer(300);
        timer.setOnTimeEnd(() -> showPopup("Time's up!"));
        timer.start();

        HBox topBar = new HBox(timer.getLabel());
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_CENTER);
        StackPane.setAlignment(topBar, Pos.TOP_LEFT);
        getChildren().add(topBar);
    }

    private void setupEffectProcessor() {
        effectProcessor = new EffectProcessor(maze, player, () -> showPopup("You Died!"), healthBar);
        effectProcessor.start();
    }

    private void setupAnimation() {
        AnimationTimer animationTimer = new AnimationTimer() {
            private long lastUpdate = 0;
            private final long frameTime = 1_000_000_000 / 120;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= frameTime) {
                    if (!isPopupActive) {
                        handleMovement();
                    }
                    draw();
                    lastUpdate = now;
                }
            }
        };
        animationTimer.start();
    }

    private void setEndCallback(Maze mazeObj) {
        Cell end = maze[mazeObj.getEndY()][mazeObj.getEndX()];
        if (end instanceof End endCell) {
            endCell.setOnReached(() -> showPopup("Victory"));
        }
    }

    private void handleMovement() {
        int dx = 0, dy = 0;
        if (pressedKeys.contains(KeyCode.W)) {
            dy -= GameManager.speed;
        }
        if (pressedKeys.contains(KeyCode.S)) {
            dy += GameManager.speed;
        }
        if (pressedKeys.contains(KeyCode.A)) {
            dx -= GameManager.speed;
        }
        if (pressedKeys.contains(KeyCode.D)) {
            dx += GameManager.speed;
        }

        movementController.movePlayer(player, dx, 0, GameManager.speed, maze); // спочатку по X
        movementController.movePlayer(player, 0, dy, GameManager.speed, maze);
    }

    public void showPopup(String text) {
        isPopupActive = true;
        timer.stop();
        effectProcessor.stop();

        Rectangle overlay = new Rectangle(canvas.getWidth(), canvas.getHeight(), Color.rgb(0, 0, 0, 0.5));
        ResultWindow popup = new ResultWindow(text, 100);
        popup.setMaxSize(300, 200);

        StackPane.setAlignment(popup, Pos.CENTER);
        getChildren().addAll(overlay, popup);
        canvas.setFocusTraversable(false);
    }

    private void draw() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;

        double px = player.getPositionX();
        double py = player.getPositionY();

        cameraOffsetX = clamp(px - centerX + TILE_SIZE / 2, 0, maze[0].length * TILE_SIZE - canvas.getWidth());
        cameraOffsetY = clamp(py - centerY + TILE_SIZE / 2, 0, maze.length * TILE_SIZE - canvas.getHeight());

        renderer.drawMaze(gc, maze, cameraOffsetX, cameraOffsetY, canvas.getWidth(), canvas.getHeight());
        renderer.drawPlayer(gc, player, cameraOffsetX, cameraOffsetY);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
