package gui.Game;

import managment.GameConfig;

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

import java.util.HashSet;
import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.CacheHint;
import javafx.scene.text.Text;
import javafx.util.Duration;
import managment.ScoreManager;
import entities.MazeObjects.Timer;

public class GameScreen extends StackPane {

    public static final Canvas canvas = new Canvas(11 * GameConfig.TILE_SIZE, 11 * GameConfig.TILE_SIZE);
    public static final GraphicsContext gc = canvas.getGraphicsContext2D();
    
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    
    private static final Text score = Instruments.createOutlinedText("Score: " + ScoreManager.getCurrentScore(), 51, 2);
    
    private final HealthBar healthBar;
    private final Player player;
    private final Timer timer;
    private final Maze maze;
    
    private boolean isPopupActive = false;
    
    private double cameraOffsetX = 0;
    private double cameraOffsetY = 0;

    private final GameRenderer renderer = new GameRenderer();
    private final MovementController movementController = new MovementController();
    private EffectProcessor effectProcessor;

    public static final Canvas fogCanvas = new Canvas(11 * GameConfig.TILE_SIZE, 11 * GameConfig.TILE_SIZE);
    public static final GraphicsContext fogGC = fogCanvas.getGraphicsContext2D();

    public GameScreen(Player player, Maze maze, Timer timer) {
        this.player = player;
        this.healthBar = new HealthBar(player);
        this.timer = timer;
        this.maze = maze;

        initializePlayerPosition();
        setupCanvasEvents();
        setupLayout();
        setupTopBar();
        setupEffectProcessor();
        setupAnimation();
        setEndCallback();

    }

    public static void updateScore() {
        score.setText("Score: " + ScoreManager.getCurrentScore());
    }

    private void initializePlayerPosition() {
        player.setPositionX(maze.getStartX() * GameConfig.TILE_SIZE + 10);
        player.setPositionY(maze.getStartY() * GameConfig.TILE_SIZE + 10);
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
        timer.setOnTimeEnd(() -> showPopup("Time's up!"));
        timer.start();

        HBox topBar = new HBox(timer.getLabel());
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_CENTER);
        StackPane.setAlignment(topBar, Pos.TOP_LEFT);
        getChildren().add(topBar);
    }

    private void setupEffectProcessor() {
        effectProcessor = new EffectProcessor(maze.getCellMaze(), player, () -> showPopup("You Died!"), healthBar);
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

    private void setEndCallback() {
        Cell end = (maze.getCellMaze())[maze.getEndY()][maze.getEndX()];
        if (end instanceof End endCell) {
            endCell.setOnReached(() -> showPopup("Victory"));
        }
    }

    private void handleMovement() {
        int dx = 0, dy = 0;
        if (pressedKeys.contains(KeyCode.W)) {
            dy -= GameConfig.SPEED;
        }
        if (pressedKeys.contains(KeyCode.S)) {
            dy += GameConfig.SPEED;
        }
        if (pressedKeys.contains(KeyCode.A)) {
            dx -= GameConfig.SPEED;
        }
        if (pressedKeys.contains(KeyCode.D)) {
            dx += GameConfig.SPEED;
        }

        movementController.movePlayer(player, dx, 0, GameConfig.SPEED, maze.getCellMaze()); // спочатку по X
        movementController.movePlayer(player, 0, dy, GameConfig.SPEED, maze.getCellMaze());
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

        cameraOffsetX = clamp(px - centerX + GameConfig.TILE_SIZE / 2, 0, maze.getCellMaze()[0].length * GameConfig.TILE_SIZE - canvas.getWidth());
        cameraOffsetY = clamp(py - centerY + GameConfig.TILE_SIZE / 2, 0, maze.getCellMaze().length * GameConfig.TILE_SIZE - canvas.getHeight());

        renderer.drawMaze(gc, maze.getCellMaze(), cameraOffsetX, cameraOffsetY, canvas.getWidth(), canvas.getHeight());
        renderer.drawPlayer(gc, player, cameraOffsetX, cameraOffsetY);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
