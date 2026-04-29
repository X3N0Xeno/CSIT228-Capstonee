package com.example.hangman;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HangmanController {

    @FXML private Label wordLabel;
    @FXML private Label statusLabel;
    @FXML private Label bulletLabel;
    @FXML private TextField inputField;
    @FXML private Button guessButton;
    @FXML private Button restartButton;
    @FXML private FlowPane keyboardPane;
    @FXML private StackPane rootPane;
    @FXML private RouletteCanvas rouletteCanvas;

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
        int numWord = (int) (Math.random() * 3103);
        WordRetriever wordRetriever = new WordRetriever(numWord);
        word = wordRetriever.getWord();
        letters = new char[word.length()];
        Arrays.fill(letters, '_');
        wrongGuess = 0;
        gameOver = false;
        spinInProgress = false;

        wordLabel.setText(formatWord());
        bulletLabel.setText("🔫 Wrong guesses: 0");
        statusLabel.setText("Guess a letter!");
        statusLabel.setStyle("-fx-text-fill: #aaaaaa;");
        inputField.clear();
        inputField.setDisable(false);
        guessButton.setDisable(false);
        restartButton.setVisible(false);

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
            bulletLabel.setText("🔫 Wrong guesses: " + wrongGuess);

            if (pressedKey != null) {
                pressedKey.setStyle(wrongKeyStyle());
                pressedKey.setDisable(true);
            }

            // Disable input during spin
            spinInProgress = true;
            inputField.setDisable(true);
            guessButton.setDisable(true);
            keyButtons.values().forEach(btn -> btn.setDisable(true));

            statusLabel.setText("🎰 Spinning...");
            statusLabel.setStyle("-fx-text-fill: #f5a623;");

            ScreenEffects.flashRed(rootPane);

            rouletteCanvas.spin(
                    // Landed on bullet → dead
                    () -> {
                        statusLabel.setText("💥 BANG! You're dead. Word was: " + word);
                        statusLabel.setStyle("-fx-text-fill: #e94560;");
                        wordLabel.setText(word);
                        ScreenEffects.shake(HangmanApplication.getStage());
                        endGame();
                    },
                    // Survived
                    () -> {
                        spinInProgress = false;
                        statusLabel.setText("*click* ... You survived. Death chance: " + (wrongGuess + 1) + "/7");
                        statusLabel.setStyle("-fx-text-fill: #f5a623;");
                        inputField.setDisable(false);
                        guessButton.setDisable(false);
                        // Re-enable only unused keys
                        keyButtons.forEach((c, btn) -> {
                            if (!btn.isDisabled()) btn.setDisable(false);
                        });
                        keyButtons.values().stream()
                                .filter(btn -> btn.getStyle().equals(defaultKeyStyle()))
                                .forEach(btn -> btn.setDisable(false));
                    }
            );

        } else {
            if (pressedKey != null) {
                pressedKey.setStyle(correctKeyStyle());
                pressedKey.setDisable(true);
            }

            if (new String(letters).equals(word)) {
                statusLabel.setText("🎉 You got it! The word was: " + word);
                statusLabel.setStyle("-fx-text-fill: #4caf50;");
                wordLabel.setText(word);
                ScreenEffects.confetti(rootPane);
                endGame();
            } else {
                statusLabel.setText("✅ Good guess!");
                statusLabel.setStyle("-fx-text-fill: #4caf50;");
            }
        }
    }

    @FXML
    private void handleRestart() { newGame(); }

    private void endGame() {
        gameOver = true;
        inputField.setDisable(true);
        guessButton.setDisable(true);
        restartButton.setVisible(true);
        keyButtons.values().forEach(btn -> btn.setDisable(true));
    }

    private String formatWord() {
        StringBuilder sb = new StringBuilder();
        for (char c : letters) sb.append(c).append(' ');
        return sb.toString().trim();
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