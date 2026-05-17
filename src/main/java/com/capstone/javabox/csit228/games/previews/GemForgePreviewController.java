package com.capstone.javabox.csit228.games.previews;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class GemForgePreviewController {

    @FXML private Pane logoCanvas;

    private final Color[] EMBER_COLORS = {
            Color.web("#ff3300", 0.7),
            Color.web("#ff6600", 0.6),
            Color.web("#ffcc00", 0.5),
            Color.web("#cc3300", 0.4)
    };

    @FXML
    public void initialize() {
        // Automatically clip the canvas to its true dynamic size
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(logoCanvas.widthProperty());
        clip.heightProperty().bind(logoCanvas.heightProperty());
        logoCanvas.setClip(clip);

        startEmberParticleEngine();
    }

    private void startEmberParticleEngine() {
        AnimationTimer particleLoop = new AnimationTimer() {
            private long lastSpawnTime = 0;

            @Override
            public void handle(long now) {
                if (now - lastSpawnTime > 80_000_000) {
                    spawnEmber();
                    lastSpawnTime = now;
                }
            }
        };
        particleLoop.start();
    }

    private void spawnEmber() {
        // Grab the ACTUAL true dimensions of the layout
        double canvasWidth = logoCanvas.getWidth();
        double canvasHeight = logoCanvas.getHeight();

        // Failsafe: Do not spawn if JavaFX hasn't calculated the layout bounds yet (first split-second of loading)
        if (canvasWidth == 0 || canvasHeight == 0) return;

        double radius = 1.5 + (Math.random() * 3.5);
        int colorIndex = (int) (Math.random() * EMBER_COLORS.length);

        Circle ember = new Circle(radius, EMBER_COLORS[colorIndex]);

        // Spawn horizontally across the dynamic width (keeping a 30px buffer from the left sidebar)
        double startX = 30 + (Math.random() * (canvasWidth - 60));

        // Spawn strictly below the TRUE bottom edge of the dynamic pane
        double startY = canvasHeight + 15;

        ember.setCenterX(startX);
        ember.setCenterY(startY);

        logoCanvas.getChildren().add(0, ember);

        double travelDuration = 1800 + (Math.random() * 1400);
        TranslateTransition rise = new TranslateTransition(Duration.millis(travelDuration), ember);

        double driftX = -15 + (Math.random() * 45);
        rise.setByX(driftX);

        // Ensure the particle drifts all the way past the top of the canvas
        rise.setByY(-(canvasHeight + 60));

        FadeTransition coolDown = new FadeTransition(Duration.millis(travelDuration), ember);
        coolDown.setToValue(0.0);

        ParallelTransition lifeCycle = new ParallelTransition(rise, coolDown);

        lifeCycle.setOnFinished(e -> logoCanvas.getChildren().remove(ember));

        lifeCycle.play();
    }
}