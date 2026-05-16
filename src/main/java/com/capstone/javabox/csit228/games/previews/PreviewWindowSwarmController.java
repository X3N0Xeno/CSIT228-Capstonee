package com.capstone.javabox.csit228.games.previews;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class PreviewWindowSwarmController {

    @FXML private StackPane window1;
    @FXML private StackPane window2;

    @FXML
    public void initialize() {
        // Create an eerie floating effect for the windows
        applyFloatAnimation(window1, -15, 3.0);
        applyFloatAnimation(window2, 20, 2.5);
    }

    private void applyFloatAnimation(StackPane window, double distance, double durationSeconds) {
        TranslateTransition floatAnim = new TranslateTransition(Duration.seconds(durationSeconds), window);
        floatAnim.setByY(distance);
        floatAnim.setCycleCount(TranslateTransition.INDEFINITE);
        floatAnim.setAutoReverse(true);
        floatAnim.play();
    }
}