package com.capstone.javabox.csit228.games.previews;

import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class BookwormBattlePreviewController {

    @FXML private StackPane previewRoot;
    @FXML private HBox word1Box;
    @FXML private HBox word2Box;

    private List<StackPane> allTiles = new ArrayList<>();

    @FXML
    public void initialize() {
        buildWord("BOOKWORM", word1Box);
        buildWord("BATTLE", word2Box);

        startTileDropAnimation();
    }

    private void buildWord(String word, HBox targetContainer) {
        for (char c : word.toCharArray()) {
            StackPane tile = createScrabbleTile(c);

            // Start the tiles high up off-screen and invisible
            tile.setTranslateY(-300);
            tile.setOpacity(0);

            allTiles.add(tile);
            targetContainer.getChildren().add(tile);
        }
    }

    private StackPane createScrabbleTile(char letter) {
        StackPane tileLayout = new StackPane();
        tileLayout.setPrefSize(70, 70);

        // 1. The Wooden Block Base
        Rectangle base = new Rectangle(70, 70);
        base.setArcWidth(12);
        base.setArcHeight(12);
        base.setFill(Color.web("#f5cd79")); // Light wood color
        base.setStroke(Color.web("#d1ccc0"));
        base.setStrokeWidth(2);
        base.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0.4, 0, 4);");

        // 2. The Main Centered Letter
        Label lblLetter = new Label(String.valueOf(letter));
        lblLetter.setStyle(
                "-fx-text-fill: #2f3542; " +
                        "-fx-font-size: 42px; " +
                        "-fx-font-family: 'Arial'; " +
                        "-fx-font-weight: bold;"
        );
        StackPane.setAlignment(lblLetter, Pos.CENTER);

        // 3. The Point Value (Bottom-Left Corner)
        int pointValue = getScrabbleScore(letter);
        Label lblScore = new Label(String.valueOf(pointValue));
        lblScore.setStyle(
                "-fx-text-fill: #2f3542; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-family: 'Arial'; " +
                        "-fx-font-weight: bold;"
        );
        // Anchor to the bottom left with a tiny bit of padding so it doesn't touch the edge
        StackPane.setAlignment(lblScore, Pos.BOTTOM_LEFT);
        lblScore.setTranslateX(6);
        lblScore.setTranslateY(-4);

        tileLayout.getChildren().addAll(base, lblLetter, lblScore);
        return tileLayout;
    }

    private void startTileDropAnimation() {
        SequentialTransition sequence = new SequentialTransition();

        for (StackPane tile : allTiles) {
            TranslateTransition drop = new TranslateTransition(Duration.millis(150), tile);
            drop.setToY(0);
            drop.setInterpolator(Interpolator.EASE_IN);

            TranslateTransition bounceUp = new TranslateTransition(Duration.millis(80), tile);
            bounceUp.setToY(-15);
            bounceUp.setInterpolator(Interpolator.EASE_OUT);

            TranslateTransition settle = new TranslateTransition(Duration.millis(80), tile);
            settle.setToY(0);
            settle.setInterpolator(Interpolator.EASE_IN);

            drop.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                if (newStatus == javafx.animation.Animation.Status.RUNNING) {
                    tile.setOpacity(1);
                }
            });

            sequence.getChildren().addAll(drop, bounceUp, settle);
        }

        // --- THE FIX ---
        // Add a 3.5-second pause directly to the end of the animation sequence!
        sequence.getChildren().add(new javafx.animation.PauseTransition(Duration.millis(3500)));

        sequence.setOnFinished(e -> {
            for (StackPane tile : allTiles) {
                tile.setTranslateY(-300);
                tile.setOpacity(0);
            }
            sequence.playFromStart(); // Safely loop it from the beginning
        });

        sequence.play();
    }

    // Classic Scrabble point distribution values
    private int getScrabbleScore(char letter) {
        switch (letter) {
            case 'A': case 'E': case 'I': case 'O': case 'U':
            case 'L': case 'N': case 'S': case 'T': case 'R': return 1;
            case 'D': case 'G': return 2;
            case 'B': case 'C': case 'M': case 'P': return 3;
            case 'F': case 'H': case 'V': case 'W': case 'Y': return 4;
            case 'K': return 5;
            case 'J': case 'X': return 8;
            case 'Q': case 'Z': return 10;
            default: return 0;
        }
    }
}