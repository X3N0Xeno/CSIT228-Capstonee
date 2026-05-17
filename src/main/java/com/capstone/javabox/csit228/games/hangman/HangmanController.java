package com.capstone.javabox.csit228.games.hangman;

import com.capstone.javabox.csit228.games.JavaboxAbstractController;
import com.capstone.javabox.csit228.database.HangmanDAO;
import com.capstone.javabox.csit228.utils.JavaboxUtils;
import com.capstone.javabox.csit228.utils.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HangmanController extends JavaboxAbstractController {

    @FXML private Label wordLabel;
    @FXML private Label statusLabel;
    @FXML private Label bulletLabel;
    @FXML private TextField inputField;
    @FXML private Button guessButton;
    @FXML private Button restartButton;
    @FXML private FlowPane keyboardPane;
    @FXML private StackPane rootPane;
    @FXML private RouletteCanvas rouletteCanvas;
    @FXML private TextField playerNameInput;
    @FXML private Button saveScoreBtn;
    private HangmanGame lastGameResult;

    // Stats panel
    @FXML private VBox statsPane;
    @FXML private Label statGames;
    @FXML private Label statWinRate;
    @FXML private Label statStreak;
    @FXML private Label statBestStreak;
    @FXML private Label statPulls;
    @FXML private Label statDeaths;

    private final HangmanStatisticsManager statsManager = new HangmanStatisticsManager();

    private String word;
    private char[] letters;
    private int wrongGuess;
    private boolean gameOver;
    private boolean spinInProgress;

    private final Map<Character, Button> keyButtons = new HashMap<>();

    @FXML
    public void initialize() {
        buildKeyboard();
        newGame();
    }

    private void buildKeyboard() {
        keyboardPane.getChildren().clear();
        keyButtons.clear();

        for (char c = 'a'; c <= 'z'; c++) {
            Button btn = new Button(String.valueOf(c).toUpperCase());
            btn.setPrefWidth(40);
            btn.setPrefHeight(40);
            btn.setStyle(defaultKeyStyle());

            final char letter = c;
            btn.setOnAction(e -> {
                inputField.setText(String.valueOf(letter));
                handleGuess();
            });

            keyButtons.put(c, btn);
            keyboardPane.getChildren().add(btn);
        }
    }

    private void newGame() {
        SoundManager.playMusic("music_jazz_unsolved_mystery.mp3");
        int numWord = (int) (Math.random() * 5761);
        WordRetriever wordRetriever = new WordRetriever(numWord);
        word = wordRetriever.getWord();
        letters = new char[word.length()];
        Arrays.fill(letters, '_');

        wrongGuess = 0;
        gameOver = false;
        spinInProgress = false;

        wordLabel.setText(formatWord());
        bulletLabel.setText("⚠️ Spared: 0 Lives");
        statusLabel.setText("Choose a letter... wisely.");
        statusLabel.setStyle("-fx-text-fill: #aaaaaa;");
        inputField.clear();
        inputField.setDisable(false);
        guessButton.setDisable(false);
        restartButton.managedProperty().bind(restartButton.visibleProperty());
        restartButton.setVisible(false);
        statsPane.setVisible(false);

        rouletteCanvas.reset();
        keyButtons.values().forEach(btn -> {
            btn.setStyle(defaultKeyStyle());
            btn.setDisable(false);
        });
    }

    @FXML
    private void handleGuess() {
        if (gameOver || spinInProgress) return;

        String input = inputField.getText().trim();
        if (input.isEmpty()) return;

        char ch = input.toLowerCase().charAt(0);
        inputField.clear();

        Button pressedKey = keyButtons.get(ch);
        if (pressedKey != null && pressedKey.isDisabled()) return;

        boolean correct = false;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == ch) {
                letters[i] = ch;
                correct = true;
            }
        }

        wordLabel.setText(formatWord());

        if (!correct) {
            wrongGuess++;
            bulletLabel.setText("⚠️ Spared: " + wrongGuess + " Lives");

            if (pressedKey != null) {
                pressedKey.setStyle(wrongKeyStyle());
                pressedKey.setDisable(true);
            }

            spinInProgress = true;
            inputField.setDisable(true);
            guessButton.setDisable(true);
            keyButtons.values().forEach(btn -> btn.setDisable(true));

            statusLabel.setText("🎲 Spinning the cylinder...");
            SoundManager.playSFX("sfx_revolver_spin.mp3");
            statusLabel.setStyle("-fx-text-fill: #f5a623;");

            ScreenEffects.flashRed(rootPane);
            //SMOOTH AHH TRANSITION RIGHT???
            rouletteCanvas.spin(
                    () -> {
                        SoundManager.playMusic("music_jazz_game_over.mp3");
                        SoundManager.playSFX("sfx_gunshot.mp3");
                        SoundManager.playSFX("sfx_death.mp3");
                        statusLabel.setText("💥 BANG! You're dead. Word: " + word.toUpperCase());
                        statusLabel.setStyle("-fx-text-fill: #e94560;");
                        wordLabel.setText(word.toUpperCase());
                        ScreenEffects.shake(JavaboxUtils.getStage(statusLabel));
                        endGame(false);
                    },
                    () -> {
                        SoundManager.playSFX("sfx_empty_gun.mp3");
                        spinInProgress = false;
                        statusLabel.setText("*click* ... Empty chamber. Survive another turn.");
                        statusLabel.setStyle("-fx-text-fill: #f5a623;");
                        inputField.setDisable(false);
                        guessButton.setDisable(false);

                        keyButtons.forEach((c, btn) -> {
                            if (!btn.getStyle().equals(wrongKeyStyle()) && !btn.getStyle().equals(correctKeyStyle())) {
                                btn.setDisable(false);
                            }
                        });
                    }
            );

        } else {
            if (pressedKey != null) {
                pressedKey.setStyle(correctKeyStyle());
                pressedKey.setDisable(true);
            }

            if (new String(letters).equals(word)) {
                SoundManager.playSFX("sfx_powerup.mp3");
                statusLabel.setText("💰 You survived. The word was: " + word.toUpperCase());
                statusLabel.setStyle("-fx-text-fill: #4caf50;");
                wordLabel.setText(word.toUpperCase());
                ScreenEffects.confetti(rootPane);
                endGame(true);
            } else {
                SoundManager.playSFX("sfx_ui_confirm.mp3");
                statusLabel.setText("🎯 Direct hit!");
                statusLabel.setStyle("-fx-text-fill: #4caf50;");
            }
        }
    }

    @FXML
    private void handleRestart() {
        SoundManager.playSFX("sfx_ui_confirm.mp3");
        SoundManager.playMusic("music_jazz_unsolved_mystery.mp3");
        newGame();
    }

    @FXML
    private void handleQuit() {
        SoundManager.playSFX("sfx_ui_accept_death.mp3");
        SoundManager.stopMusic();
        quitToLobby();
    }

    private void endGame(boolean win) {
        gameOver = true;
        inputField.setDisable(true);
        guessButton.setDisable(true);
        restartButton.setVisible(true);
        keyButtons.values().forEach(btn -> btn.setDisable(true));

        int pullsSurvived = win ? wrongGuess : wrongGuess - 1;
        // Save to global variable so the upload button can grab it
        lastGameResult = new HangmanGame(word, win, Math.max(0, pullsSurvived));

        statsManager.recordGame(lastGameResult);
        showStats();

        // Reset the Database Upload UI for the new game
        if (playerNameInput != null) {
            playerNameInput.setDisable(false);
            playerNameInput.clear();
            saveScoreBtn.setDisable(false);
            saveScoreBtn.setText("Upload");
        }
    }

    @FXML
    private void handleSaveScore() {
        if (lastGameResult == null) return;

        String alias = playerNameInput.getText().trim();
        if (!alias.isEmpty()) {
            HangmanDAO.saveGame(alias, lastGameResult.isWin(), lastGameResult.getPullsSurvived());

            // Lock the UI so they don't spam upload
            saveScoreBtn.setText("Uploaded!");
            saveScoreBtn.setDisable(true);
            playerNameInput.setDisable(true);
        }
    }

    private void showStats() {
        statGames.setText("Games Played: " + statsManager.getTotalGamesPlayed());
        statWinRate.setText(String.format("Win Rate: %.1f%%", statsManager.getWinRate()));
        statStreak.setText("Current Streak: " + statsManager.getCurrentStreak());
        statBestStreak.setText("Best Streak: " + statsManager.getBestStreak());
        statPulls.setText("🔫 Pulls Survived: " + statsManager.getTotalPullsSurvived());
        statDeaths.setText("💀 Times Shot: " + statsManager.getTotalDeaths());
        statsPane.setVisible(true);
    }

    private String formatWord() {
        StringBuilder sb = new StringBuilder();
        for (char c : letters) sb.append(c).append(' ');
        return sb.toString().toUpperCase().trim();
    }

    private String defaultKeyStyle() {
        return "-fx-font-family: Monospace; -fx-font-size: 13; " +
                "-fx-background-color: #16213e; -fx-text-fill: #eaeaea; " +
                "-fx-border-color: #e94560; -fx-border-radius: 4; " +
                "-fx-background-radius: 4; -fx-cursor: hand;";
    }

    private String wrongKeyStyle() {
        return "-fx-font-family: Monospace; -fx-font-size: 13; " +
                "-fx-background-color: #e94560; -fx-text-fill: white; " +
                "-fx-border-color: #e94560; -fx-border-radius: 4; " +
                "-fx-background-radius: 4;";
    }

    private String correctKeyStyle() {
        return "-fx-font-family: Monospace; -fx-font-size: 13; " +
                "-fx-background-color: #4caf50; -fx-text-fill: white; " +
                "-fx-border-color: #4caf50; -fx-border-radius: 4; " +
                "-fx-background-radius: 4;";
    }
}