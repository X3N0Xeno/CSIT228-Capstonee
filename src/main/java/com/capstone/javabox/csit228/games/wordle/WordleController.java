package com.capstone.javabox.csit228.games.wordle;

import com.capstone.javabox.csit228.games.JavaboxAbstractController;
import com.capstone.javabox.csit228.utils.SoundManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.util.Arrays;

public class WordleController extends JavaboxAbstractController {

    @FXML private GridPane guessGrid;
    @FXML private TextField inputField;
    @FXML private Button submitButton;
    @FXML private Button restartButton;
    @FXML private Label statusLabel;
    @FXML private VBox statsPane;
    @FXML private Label statWinRate;
    @FXML private Label statStreak;
    @FXML private Label statBestStreak;
    @FXML private Label statGames;
    @FXML private VBox distributionChart;

    private static final int MAX_GUESSES = 8;
    private static final int WORD_LENGTH = 5;

    private WordValidator validator;
    private StatisticsManager statsManager;

    private String secretWord;
    private int currentRow = 0;
    private boolean gameOver = false;

    private String[] guesses;
    private int[] correctCounts;
    private int[] misplacedCounts;
    private char[] lockedGreens;

    private Label[][] tiles;
    private Label[] feedbackLabels;

    @FXML
    public void initialize() {
        validator = new WordValidator();
        new Thread(validator).start();
        statsManager = new StatisticsManager();

        tiles = new Label[MAX_GUESSES][WORD_LENGTH];
        feedbackLabels = new Label[MAX_GUESSES];
        buildGrid();
        newGame();
    }

    private void buildGrid() {
        guessGrid.getChildren().clear();
        guessGrid.setHgap(8);
        guessGrid.setVgap(8);

        for (int row = 0; row < MAX_GUESSES; row++) {
            for (int col = 0; col < WORD_LENGTH; col++) {
                Label tile = new Label(" ");
                tile.setPrefSize(52, 52);
                tile.setAlignment(Pos.CENTER);
                tile.setFont(Font.font("Monospace", 22));
                tile.setTextFill(Color.web("#eaeaea"));
                tile.setStyle(emptyTileStyle());
                tiles[row][col] = tile;
                guessGrid.add(tile, col, row);
            }

            Label feedback = new Label("");
            feedback.setFont(Font.font("Monospace", 13));
            feedback.setTextFill(Color.web("#aaaaaa"));
            feedback.setAlignment(Pos.CENTER_LEFT);
            feedbackLabels[row] = feedback;
            guessGrid.add(feedback, 6, row);
        }
    }

    private void newGame() {
        int numWord = (int) (Math.random() * 5757);
        WordRetriever retriever = new WordRetriever(numWord);
        secretWord = retriever.getWord().toLowerCase().trim();

        currentRow = 0;
        gameOver = false;
        guesses = new String[MAX_GUESSES];
        correctCounts = new int[MAX_GUESSES];
        misplacedCounts = new int[MAX_GUESSES];
        lockedGreens = new char[WORD_LENGTH];

        buildGrid();
        inputField.clear();
        inputField.setDisable(false);
        submitButton.setDisable(false);
        restartButton.setVisible(false);
        statsPane.setVisible(false);
        statusLabel.setText("Guess the 5-letter word!");
        statusLabel.setStyle("-fx-text-fill: #aaaaaa;");
    }

    @FXML
    private void handleSubmit() {
        if (gameOver) return;

        String input = inputField.getText().trim().toLowerCase();
        inputField.clear();

        if (input.length() != WORD_LENGTH) {
            statusLabel.setText("⚠ Must be exactly 5 letters!");
            statusLabel.setStyle("-fx-text-fill: #f5a623;");
            return;
        }

        if (!validator.isValid(input)) {
            statusLabel.setText("⚠ Not a valid word!");
            statusLabel.setStyle("-fx-text-fill: #f5a623;");
            return;
        }

        String hardModeError = validateHardMode(input);
        if (hardModeError != null) {
            statusLabel.setText(hardModeError);
            statusLabel.setStyle("-fx-text-fill: #f5a623;");
            return;
        }

        int correct = countCorrect(input);
        int misplaced = countMisplaced(input);

        guesses[currentRow] = input;
        correctCounts[currentRow] = correct;
        misplacedCounts[currentRow] = misplaced;

        for (int col = 0; col < WORD_LENGTH; col++) {
            char letter = input.charAt(col);
            tiles[currentRow][col].setText(String.valueOf(letter).toUpperCase());

            if (letter == secretWord.charAt(col)) {
                tiles[currentRow][col].setStyle(greenTileStyle());
                lockedGreens[col] = letter;
            } else {
                tiles[currentRow][col].setStyle(neutralTileStyle());
            }
        }

        feedbackLabels[currentRow].setText("🔄 " + misplaced + " misplaced");

        if (correct == WORD_LENGTH) {
            SoundManager.playSFX("sfx_powerup.mp3");
            statusLabel.setText("🎉 Got it in " + (currentRow + 1) + " guess" + (currentRow == 0 ? "" : "es") + "!");
            statusLabel.setStyle("-fx-text-fill: #4caf50;");
            endGame(true);
            return;
        }

        currentRow++;

        if (currentRow >= MAX_GUESSES) {
            statusLabel.setText("💀 Out of guesses! Word was: " + secretWord.toUpperCase());
            statusLabel.setStyle("-fx-text-fill: #e94560;");
            endGame(false);
            return;
        }

        SoundManager.playSFX("sfx_ui_confirm.mp3");
        statusLabel.setText("Guess " + (currentRow + 1) + " of " + MAX_GUESSES);
        statusLabel.setStyle("-fx-text-fill: #aaaaaa;");
    }

    private String validateHardMode(String input) {
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (lockedGreens[i] != '\0' && input.charAt(i) != lockedGreens[i]) {
                return "⚠ Position " + (i + 1) + " must be '"
                        + String.valueOf(lockedGreens[i]).toUpperCase() + "'!";
            }
        }
        return null;
    }

    private void endGame(boolean win) {
        gameOver = true;
        inputField.setDisable(true);
        submitButton.setDisable(true);
        restartButton.setVisible(true);

        WordleGame game = new WordleGame(
                secretWord, win,
                win ? currentRow + 1 : MAX_GUESSES,
                Arrays.copyOf(guesses, currentRow + (win ? 1 : 0)),
                Arrays.copyOf(correctCounts, currentRow + (win ? 1 : 0)),
                Arrays.copyOf(misplacedCounts, currentRow + (win ? 1 : 0))
        );

        statsManager.recordGame(game);
        showStats();
    }

    private void showStats() {
        statGames.setText("Games Played: " + statsManager.getTotalGamesPlayed());
        statWinRate.setText(String.format("Win Rate: %.1f%%", statsManager.getWinRate()));
        statStreak.setText("Current Streak: " + statsManager.getCurrentStreak());
        statBestStreak.setText("Best Streak: " + statsManager.getBestStreak());
        buildDistributionChart();
        statsPane.setVisible(true);
    }

    private void buildDistributionChart() {
        distributionChart.getChildren().clear();
        int[] dist = statsManager.getGuessDistribution();
        int max = Arrays.stream(dist).max().orElse(1);
        if (max == 0) max = 1;

        for (int i = 0; i < MAX_GUESSES; i++) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            Label guessNum = new Label((i + 1) + " ");
            guessNum.setFont(Font.font("Monospace", 13));
            guessNum.setTextFill(Color.web("#aaaaaa"));
            guessNum.setMinWidth(20);

            double barWidth = (dist[i] / (double) max) * 180;
            Rectangle bar = new Rectangle(Math.max(barWidth, 4), 22);
            bar.setFill(Color.web(dist[i] > 0 ? "#4caf50" : "#16213e"));
            bar.setArcWidth(4);
            bar.setArcHeight(4);

            Label count = new Label(" " + dist[i]);
            count.setFont(Font.font("Monospace", 13));
            count.setTextFill(Color.web("#eaeaea"));

            row.getChildren().addAll(guessNum, bar, count);
            distributionChart.getChildren().add(row);
        }
    }

    private int countCorrect(String guess) {
        int count = 0;
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (guess.charAt(i) == secretWord.charAt(i)) count++;
        }
        return count;
    }

    private int countMisplaced(String guess) {
        int[] secretFreq = new int[26];
        int[] guessFreq  = new int[26];

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (guess.charAt(i) != secretWord.charAt(i)) {
                secretFreq[secretWord.charAt(i) - 'a']++;
                guessFreq[guess.charAt(i) - 'a']++;
            }
        }

        int misplaced = 0;
        for (int i = 0; i < 26; i++) {
            misplaced += Math.min(secretFreq[i], guessFreq[i]);
        }
        return misplaced;
    }

    @FXML
    private void handleRestart() {
        SoundManager.playSFX("sfx_ui_confirm.mp3");
        newGame();
    }

    @FXML
    private void handleQuit() {
        SoundManager.playSFX("sfx_ui_accept_death.mp3");
        quitToLobby();
    }

    private String emptyTileStyle() {
        return "-fx-background-color: #16213e; " +
                "-fx-border-color: #444466; " +
                "-fx-border-radius: 4; -fx-background-radius: 4;";
    }

    private String neutralTileStyle() {
        return "-fx-background-color: #0f3460; " +
                "-fx-border-color: #555588; " +
                "-fx-border-radius: 4; -fx-background-radius: 4;";
    }

    private String greenTileStyle() {
        return "-fx-background-color: #4caf50; " +
                "-fx-border-color: #4caf50; " +
                "-fx-border-radius: 4; -fx-background-radius: 4;";
    }
}