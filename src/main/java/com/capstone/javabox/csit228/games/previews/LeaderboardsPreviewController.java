package com.capstone.javabox.csit228.games.previews;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardsPreviewController {

    @FXML private StackPane previewRoot;
    @FXML private Pane bgCanvas;
    @FXML private HBox textBoxLine1;
    @FXML private HBox textBoxLine2;

    private final int TILE_SIZE = 60;
    private final Color GOLD_LIGHT = Color.web("#FFD700");
    private final Color GOLD_DARK = Color.web("#DAA520");

    // Store all letters in a single flat list so the wave passes through them seamlessly
    private List<Node> allLetters = new ArrayList<>();

    @FXML
    public void initialize() {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(previewRoot.widthProperty());
        clip.heightProperty().bind(previewRoot.heightProperty());
        previewRoot.setClip(clip);

        buildCheckerboard();

        // Build the two separate lines
        buildWavyTextLine("HALL", textBoxLine1);
        buildWavyTextLine("OF FAME", textBoxLine2);

        startKineticEngine();
    }

    private void buildCheckerboard() {
        int cols = 40;
        int rows = 25;

        for (int r = -5; r < rows; r++) {
            for (int c = -5; c < cols; c++) {
                Rectangle rect = new Rectangle(TILE_SIZE, TILE_SIZE);

                if ((r + c) % 2 == 0) {
                    rect.setFill(GOLD_LIGHT);
                } else {
                    rect.setFill(GOLD_DARK);
                }

                rect.setX(c * TILE_SIZE);
                rect.setY(r * TILE_SIZE);
                bgCanvas.getChildren().add(rect);
            }
        }
    }

    private void buildWavyTextLine(String text, HBox targetContainer) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == ' ') {
                // Invisible spacer for the gap
                Rectangle space = new Rectangle(25, 10, Color.TRANSPARENT);
                targetContainer.getChildren().add(space);
                allLetters.add(space); // Add to list to keep the wave phase perfectly timed
            } else {
                Label letter = new Label(String.valueOf(c));
                letter.setStyle(
                        "-fx-text-fill: white; " +
                                "-fx-font-size: 85px; " +
                                "-fx-font-family: 'Georgia'; " +
                                "-fx-font-weight: bold; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.6), 12, 0.5, 0, 6);"
                );
                targetContainer.getChildren().add(letter);
                allLetters.add(letter);
            }
        }
    }

    private void startKineticEngine() {
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double time = now / 1_000_000_000.0;

                double bgSpeedX = 15.0;
                double bgSpeedY = 40.0;

                double shiftX = (time * bgSpeedX) % (TILE_SIZE * 2);
                double shiftY = -(time * bgSpeedY) % (TILE_SIZE * 2);

                bgCanvas.setTranslateX(shiftX);
                bgCanvas.setTranslateY(shiftY);

                double waveSpeed = 3.0;
                double waveHeight = 20.0;
                double letterOffset = 0.4;

                for (int i = 0; i < allLetters.size(); i++) {
                    Node letter = allLetters.get(i);
                    double yOffset = Math.sin((time * waveSpeed) + (i * letterOffset)) * waveHeight;
                    letter.setTranslateY(yOffset);
                }
            }
        };
        loop.start();
    }
}