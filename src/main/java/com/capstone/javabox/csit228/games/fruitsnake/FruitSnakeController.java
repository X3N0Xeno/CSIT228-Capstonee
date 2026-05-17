package com.capstone.javabox.csit228.games.fruitsnake;

import com.capstone.javabox.csit228.database.FruitSnakeDAO;
import com.capstone.javabox.csit228.games.JavaboxAbstractController;
import com.capstone.javabox.csit228.utils.SoundManager;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Deque;

public class FruitSnakeController extends JavaboxAbstractController {

    @FXML private StackPane rootPane;
    @FXML private VBox setupScene;
    @FXML private VBox gameScene;
    @FXML private VBox gameOverScene;
    @FXML private Canvas gameCanvas;
    @FXML private Label scoreLabel;
    @FXML private Label roundLabel;
    @FXML private Label lengthLabel;
    @FXML private Label finalScoreLabel;
    @FXML private Label finalRoundsLabel;
    @FXML private Label highScoreLabel;
    @FXML private Label highRoundsLabel;
    @FXML private Label roundWonLabel;

    // --- NEW: DATABASE UI VARIABLES ---
    @FXML private TextField playerNameInput;
    @FXML private Button saveScoreBtn;

    private static final int TILE = 30;
    private static final double BASE_SNAKE_INTERVAL = 0.25; // seconds per snake move
    private static final double SPEED_INCREASE_PER_ROUND = 0.03;
    private static final double MIN_SNAKE_INTERVAL = 0.08;

    private FruitSnakeGame game;
    private FruitSnakeScoreManager scoreManager;
    private AnimationTimer gameLoop;

    private FruitSnakeGame.Direction pendingFruitDir = null;
    private FruitSnakeGame.Direction lastFruitDir = FruitSnakeGame.Direction.RIGHT;

    private long lastSnakeMoveTime = 0;
    private double snakeInterval;

    private int elapsedSeconds = 0;
    private long lastSecondTime = 0;
    private boolean paused = false;

    @FXML
    public void initialize() {
        SoundManager.playMusic("music_fruit_snake_menu.mp3");
        scoreManager = new FruitSnakeScoreManager();
        showScene(setupScene);
    }

    @FXML
    private void handleStartGame() {
        SoundManager.playMusic("music_fruit_snake_game.mp3");
        game = new FruitSnakeGame();
        snakeInterval = BASE_SNAKE_INTERVAL;
        elapsedSeconds = 0;
        pendingFruitDir = null;
        lastFruitDir = FruitSnakeGame.Direction.RIGHT;
        paused = false;

        // Reset database UI for the new game
        if (playerNameInput != null) {
            playerNameInput.setDisable(false);
            playerNameInput.clear();
            saveScoreBtn.setDisable(false);
            saveScoreBtn.setText("Upload");
        }

        setupKeyHandler();
        startGameLoop();
        showScene(gameScene);

        // Focus canvas for key input
        gameCanvas.setFocusTraversable(true);
        gameCanvas.requestFocus();
    }

    private void setupKeyHandler() {
        rootPane.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            switch (code) {
                case UP,    W -> pendingFruitDir = FruitSnakeGame.Direction.UP;
                case DOWN,  S -> pendingFruitDir = FruitSnakeGame.Direction.DOWN;
                case LEFT,  A -> pendingFruitDir = FruitSnakeGame.Direction.LEFT;
                case RIGHT, D -> pendingFruitDir = FruitSnakeGame.Direction.RIGHT;
                case ESCAPE   -> paused = !paused;
                default -> {}
            }
        });
    }

    private void startGameLoop() {
        if (gameLoop != null) gameLoop.stop();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (paused) return;

                // Track elapsed seconds
                if (lastSecondTime == 0) lastSecondTime = now;
                if (now - lastSecondTime >= 1_000_000_000L) {
                    elapsedSeconds++;
                    lastSecondTime = now;
                    updateHUD();
                }

                // Move fruit on key press
                if (pendingFruitDir != null) {
                    game.moveFruit(pendingFruitDir);
                    lastFruitDir = pendingFruitDir;
                    pendingFruitDir = null;
                }

                // Move snake on interval
                if (lastSnakeMoveTime == 0) lastSnakeMoveTime = now;
                double elapsed = (now - lastSnakeMoveTime) / 1_000_000_000.0;

                if (elapsed >= snakeInterval) {
                    lastSnakeMoveTime = now;

                    int[] fruit = game.getFruitPos();
                    FruitSnakeGame.Direction aiDir = SnakeAI.getNextMove(
                            game, fruit[1], fruit[0]
                    );
                    game.moveSnake(aiDir);
                }

                // Check states
                if (game.isRoundWon()) {
                    handleRoundWon();
                    return;
                }

                if (game.isGameOver()) {
                    handleGameOver();
                    return;
                }

                render();
            }
        };

        lastSnakeMoveTime = 0;
        lastSecondTime = 0;
        gameLoop.start();
    }

    private void handleRoundWon() {
        SoundManager.playSFX("sfx_powerup.mp3");
        roundWonLabel.setText("🎉 Round " + (game.getRound() + 1) + " cleared! New snake incoming...");
        roundWonLabel.setVisible(true);

        // Speed up snake for next round
        snakeInterval = Math.max(
                BASE_SNAKE_INTERVAL - (game.getRound() * SPEED_INCREASE_PER_ROUND),
                MIN_SNAKE_INTERVAL
        );

        game.startNewRound();
        lastSnakeMoveTime = 0;

        // Hide round won label after 1.5s
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override public void run() {
                javafx.application.Platform.runLater(() ->
                        roundWonLabel.setVisible(false));
            }
        }, 1500);

        updateHUD();
        render();
    }

    private void handleGameOver() {
        gameLoop.stop();
        SoundManager.playSFX("sfx_death.mp3");

        scoreManager.recordScore(elapsedSeconds, game.getRound());

        finalScoreLabel.setText("⏱ Time Survived: " + elapsedSeconds + "s");
        finalRoundsLabel.setText("🐍 Rounds Cleared: " + game.getRound());
        highScoreLabel.setText("🏆 Best Time: " + scoreManager.getHighScore() + "s");
        highRoundsLabel.setText("🏅 Best Rounds: " + scoreManager.getHighRounds());

        showScene(gameOverScene);
    }

    @FXML
    private void handleSaveScore() {
        String alias = playerNameInput.getText().trim();
        if (!alias.isEmpty()) {
            FruitSnakeDAO.saveScore(alias, elapsedSeconds, game.getRound());

            // Lock the UI so they don't spam upload
            saveScoreBtn.setText("Uploaded!");
            saveScoreBtn.setDisable(true);
            playerNameInput.setDisable(true);
        }
    }

    private void updateHUD() {
        scoreLabel.setText("⏱ " + elapsedSeconds + "s");
        roundLabel.setText("Round: " + game.getRound());
        lengthLabel.setText("Snake: " + game.getCurrentSnakeLength()
                + " / " + FruitSnakeGame.MAX_SNAKE_LENGTH);
    }

    // --- Rendering ---

    private void render() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Background grid
        for (int r = 0; r < FruitSnakeGame.ROWS; r++) {
            for (int c = 0; c < FruitSnakeGame.COLS; c++) {
                gc.setFill((r + c) % 2 == 0
                        ? Color.web("#16213e")
                        : Color.web("#1a2444"));
                gc.fillRect(c * TILE, r * TILE, TILE, TILE);
            }
        }

        // Obstacles
        gc.setFill(Color.web("#555588"));
        for (int[] obs : game.getObstacles()) {
            gc.fillRoundRect(obs[1] * TILE + 2, obs[0] * TILE + 2,
                    TILE - 4, TILE - 4, 6, 6);
            // Rock texture lines
            gc.setStroke(Color.web("#333355"));
            gc.setLineWidth(1);
            gc.strokeLine(obs[1] * TILE + 6,  obs[0] * TILE + TILE / 2,
                    obs[1] * TILE + TILE - 6, obs[0] * TILE + TILE / 2);
        }

        // Snake
        Deque<int[]> snake = game.getSnake();
        boolean isHead = true;
        int idx = 0;
        int size = snake.size();
        for (int[] cell : snake) {
            double ratio = 1.0 - ((double) idx / size) * 0.6;
            if (isHead) {
                gc.setFill(Color.web("#e94560"));
                isHead = false;
            } else {
                // Gradient from bright to dark along body
                int r = (int)(233 * ratio);
                int g = (int)(69  * ratio);
                int b = (int)(96  * ratio);
                gc.setFill(Color.rgb(Math.min(r, 255), Math.min(g, 255), Math.min(b, 255)));
            }
            gc.fillRoundRect(cell[1] * TILE + 2, cell[0] * TILE + 2,
                    TILE - 4, TILE - 4, 8, 8);
            idx++;
        }

        // Snake eyes on head
        if (!snake.isEmpty()) {
            int[] head = snake.peekFirst();
            gc.setFill(Color.WHITE);
            gc.fillOval(head[1] * TILE + 7,  head[0] * TILE + 7,  5, 5);
            gc.fillOval(head[1] * TILE + 18, head[0] * TILE + 7,  5, 5);
            gc.setFill(Color.BLACK);
            gc.fillOval(head[1] * TILE + 8,  head[0] * TILE + 8,  3, 3);
            gc.fillOval(head[1] * TILE + 19, head[0] * TILE + 8,  3, 3);
        }

        // Fruit (player) — glowing apple emoji style
        int[] fruit = game.getFruitPos();
        gc.setFill(Color.web("#4caf50"));
        gc.fillOval(fruit[1] * TILE + 4, fruit[0] * TILE + 4, TILE - 8, TILE - 8);
        gc.setFill(Color.web("#81c784"));
        gc.fillOval(fruit[1] * TILE + 8, fruit[0] * TILE + 8, 8, 8);

        // Fruit stem
        gc.setStroke(Color.web("#4caf50"));
        gc.setLineWidth(2);
        gc.strokeLine(fruit[1] * TILE + TILE / 2, fruit[0] * TILE + 4,
                fruit[1] * TILE + TILE / 2 + 4, fruit[0] * TILE);
    }

    @FXML
    private void handlePlayAgain() {
        SoundManager.playMusic("music_fruit_snake_game.mp3");
        SoundManager.playSFX("sfx_ui_confirm.mp3");
        handleStartGame();
    }

    @FXML
    private void handleQuit() {
        if (gameLoop != null) gameLoop.stop();
        SoundManager.playSFX("sfx_ui_accept_death.mp3");
        SoundManager.playMusic(true, "music_lobby_music1.mp3", "music_lobby_music2.mp3");
        quitToLobby();
    }

    private void showScene(VBox scene) {
        for (VBox s : new VBox[]{setupScene, gameScene, gameOverScene}) {
            s.setVisible(false);
            s.setManaged(false);
        }
        scene.setVisible(true);
        scene.setManaged(true);
        if (scene == gameScene) {
            javafx.application.Platform.runLater(() -> rootPane.requestFocus());
        }
    }
}