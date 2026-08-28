package gui.game;

import management.GameConfig;

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

import javafx.scene.CacheHint;
import javafx.scene.text.Text;
import management.ScoreManager;
import game.GameTimer;
import game.effects.FogEffect;
import entities.Tiles.EndTile;
import entities.Tiles.Tile;
import game.GameState;

public class GameScreen extends StackPane {

    private final Canvas canvas = new Canvas(
            GameConfig.VIEW_SIZE * GameConfig.TILE_SIZE,
            GameConfig.VIEW_SIZE * GameConfig.TILE_SIZE);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
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

        private final Canvas fogCanvas = new Canvas(
            GameConfig.VIEW_SIZE * GameConfig.TILE_SIZE,
            GameConfig.VIEW_SIZE * GameConfig.TILE_SIZE);
        private final GraphicsContext fogGC = fogCanvas.getGraphicsContext2D();

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
            GameConfig.SCORE_FONT_SIZE,
            GameConfig.SCORE_BORDER_SIZE);

        this.timerText = Instruments.createOutlinedText(
                timer.getFormattedTime(),
            GameConfig.TIMER_FONT_SIZE,
            GameConfig.TIMER_BORDER_SIZE);
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
        player.setPositionX(maze.getStartX() * GameConfig.TILE_SIZE + GameConfig.PLAYER_SPAWN_OFFSET);
        player.setPositionY(maze.getStartY() * GameConfig.TILE_SIZE + GameConfig.PLAYER_SPAWN_OFFSET);
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

        healthBar.setPrefSize(GameConfig.HEALTH_BAR_WIDTH, GameConfig.HEALTH_BAR_HEIGHT);
        healthBar.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(healthBar, Pos.TOP_RIGHT);
        StackPane.setMargin(healthBar, new Insets(GameConfig.HUD_MARGIN));
        getChildren().add(healthBar);

        StackPane.setAlignment(scoreText, Pos.TOP_LEFT);

        StackPane.setMargin(scoreText, new Insets(GameConfig.HUD_MARGIN));
        getChildren().add(scoreText);
    }

    private void setupTopBar() {
        timer.setOnTimeEnd(() -> showPopup("Time's up!"));

        HBox topBar = new HBox(timerText);
        topBar.setPadding(new Insets(GameConfig.TOP_BAR_PADDING));
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
                Color.color(0, 0, 0, GameConfig.OVERLAY_OPACITY));

        ResultWindow popup = new ResultWindow(
                text,
                currentScore,
                () -> ScreenManager.getInstance().switchScreen("menu"),
                () -> ScreenManager.getInstance().switchScreen("game"));

        popup.setMaxSize(GameConfig.POPUP_WIDTH, GameConfig.POPUP_HEIGHT);
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
