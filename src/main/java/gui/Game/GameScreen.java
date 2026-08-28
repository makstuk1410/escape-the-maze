package gui.Game;

import managment.GameConfig;

import entities.MazeObjects.Player;
import entities.MazeObjects.Levels;
import entities.MazeObjects.Maze;
import gui.Instruments;
import gui.mainScreens.ScreenManager;
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
import game.GameTimer;
import game.effects.FogEffect;
import entities.Tiles.EndTile;
import entities.Tiles.Tile;
import game.GameState;

public class GameScreen extends StackPane {

    public static final Canvas canvas = new Canvas(11 * GameConfig.TILE_SIZE, 11 * GameConfig.TILE_SIZE);
    public static final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final InputController inputController = new InputController();

    private final Text scoreText;
    private final Text timerText;
    private final HealthBar healthBar;
    private final Player player;
    private final GameTimer timer;
    private final Maze maze;
    private final GameState gameState;
    private final FogEffect fogEffect;

    private boolean isPopupActive = false;

    private final Camera camera;

    private final GameRenderer renderer = new GameRenderer();
    private final PlayerController playerController;
    private EffectProcessor effectProcessor;
    private GameLoop gameLoop;

    public static final Canvas fogCanvas = new Canvas(11 * GameConfig.TILE_SIZE, 11 * GameConfig.TILE_SIZE);
    public static final GraphicsContext fogGC = fogCanvas.getGraphicsContext2D();

    public GameScreen(GameState gameState, Maze maze, GameTimer timer) {
        this.gameState = gameState;
        this.player = gameState.getPlayer();
        this.healthBar = new HealthBar(player);
        this.timer = timer;
        this.maze = maze;
        this.playerController = new PlayerController(player);
        this.fogEffect = new FogEffect(fogGC);
        this.scoreText = Instruments.createOutlinedText(
                "Score: 0",
                51,
                2);

        this.timerText = Instruments.createOutlinedText(
                timer.getFormattedTime(),
                40,
                1);
        this.camera = new Camera(
                canvas.getWidth(),
                canvas.getHeight());
        initializePlayerPosition();
        setupCanvasEvents();
        setupLayout();
        setupTopBar();
        setupEffectProcessor();
        setupGameLoop();
        setEndCallback();

    }



    private void setupGameLoop() {
        gameLoop = new GameLoop(deltaTime -> {

            if (!isPopupActive) {
                handleMovement();
                updateTimer(deltaTime);
                if (gameState.isFogActive()) {
                    fogEffect.start();
                }
            }

            draw();
        });

        gameLoop.start();
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
                inputController.press(e.getCode());
            }

            if (e.getCode() == KeyCode.SPACE) {
                playerController.jump();
            }
        });

        canvas.setOnKeyReleased(e -> {
            if (!isPopupActive) {
                inputController.release(e.getCode());
            }
        });
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

        StackPane.setAlignment(scoreText, Pos.TOP_LEFT);

        StackPane.setMargin(scoreText, new Insets(50));
        getChildren().add(scoreText);
    }

    private void setupTopBar() {
        timer.setOnTimeEnd(() -> showPopup("Time's up!"));

        HBox topBar = new HBox(timerText);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_CENTER);

        StackPane.setAlignment(topBar, Pos.TOP_LEFT);

        getChildren().add(topBar);
    }

    private void setupEffectProcessor() {
        effectProcessor = new EffectProcessor(maze.getTileMaze(), gameState, () -> showPopup("You Died!"), healthBar);
        effectProcessor.start();
    }

    private void setEndCallback() {
        Tile end = (maze.getTileMaze())[maze.getEndY()][maze.getEndX()];
        if (end instanceof EndTile endCell) {
            endCell.setOnReached(() -> showPopup("Victory"));
        }
    }

    private void handleMovement() {
        playerController.move(
                inputController.getPressedKeys(),
                maze.getTileMaze(),
                gameState);
    }

    public void showPopup(String text) {
        isPopupActive = true;

        gameLoop.stop();
        effectProcessor.stop();

        int currentScore = gameState.getScore();

        ScoreManager.updateScore(
                Levels.getLevel(Levels.chosenLevel).getName(),
                currentScore);

        Rectangle overlay = new Rectangle(
                canvas.getWidth(),
                canvas.getHeight(),
                Color.rgb(0, 0, 0, 0.5));

        ResultWindow popup = new ResultWindow(
                text,
                currentScore,
                () -> ScreenManager.getInstance().switchScreen("menu"),
                () -> ScreenManager.getInstance().switchScreen("game"));

        popup.setMaxSize(300, 200);
        StackPane.setAlignment(popup, Pos.CENTER);
        getChildren().addAll(overlay, popup);
        canvas.setFocusTraversable(false);
    }

    private void draw() {
        gc.clearRect(
                0,
                0,
                canvas.getWidth(),
                canvas.getHeight());
        camera.update(
                player,
                maze.getTileMaze()[0].length,
                maze.getTileMaze().length);
        renderer.drawMaze(
                gc,
                maze.getTileMaze(),
                camera.getOffsetX(),
                camera.getOffsetY(),
                canvas.getWidth(),
                canvas.getHeight());
        renderer.drawPlayer(
                gc,
                player,
                camera.getOffsetX(),
                camera.getOffsetY());
    }

    private void updateTimer(double deltaTime) {
        if (timer.update(deltaTime)) {
            timerText.setText(timer.getFormattedTime());
        }
    }
}
