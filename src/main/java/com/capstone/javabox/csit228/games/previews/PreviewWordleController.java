package com.capstone.javabox.csit228.games.previews;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class PreviewWordleController {

    // Grabs the entire row of letters at once
    @FXML private HBox letterContainer;

    @FXML
    public void initialize() {
        int index = 0;

        for (Node letterBlock : letterContainer.getChildren()) {

            // 1. Calculate the Jitter (The Disorganization)
            // Allows the blocks to float slightly higher or lower than each other
            double randomDistanceVariance = (Math.random() * 15) - 7.5;
            // Makes some blocks float slightly faster or slower
            double randomDurationVariance = (Math.random() * 0.4) - 0.2;

            // 2. Set up the physics
            TranslateTransition floatAnim = new TranslateTransition(Duration.seconds(1.2 + randomDurationVariance), letterBlock);
            floatAnim.setByY(-25.0 + randomDistanceVariance); // Move up
            floatAnim.setCycleCount(TranslateTransition.INDEFINITE);
            floatAnim.setAutoReverse(true); // Float back down smoothly

            // 3. The Wave Math
            // Multiply the index by 0.15s so the 'O' starts after the 'W', 'R' after 'O', etc.
            double sequentialDelay = index * 0.15;
            // Add a tiny random jitter so the wave isn't mathematically perfect
            double jitter = (Math.random() * 0.1);

            floatAnim.setDelay(Duration.seconds(sequentialDelay + jitter));
            floatAnim.play();

            index++;
        }
    }
}