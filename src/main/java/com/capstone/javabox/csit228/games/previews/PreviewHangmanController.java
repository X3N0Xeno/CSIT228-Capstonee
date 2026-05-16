package com.capstone.javabox.csit228.games.previews;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class PreviewHangmanController {

    @FXML private Canvas rouletteCanvas;

    private static final int CHAMBERS = 6;
    private static final double RADIUS = 250; // Scaled up from 60
    private static final double CHAMBER_RADIUS = 55; // Scaled up from 12

    @FXML
    public void initialize() {
        drawCylinder();

        // Create a continuous, menacing slow spin for the preview background
        RotateTransition spin = new RotateTransition(Duration.seconds(20), rouletteCanvas);
        spin.setByAngle(360);
        spin.setCycleCount(Animation.INDEFINITE);
        spin.setInterpolator(Interpolator.LINEAR);
        spin.play();
    }

    private void drawCylinder() {
        GraphicsContext gc = rouletteCanvas.getGraphicsContext2D();
        double cx = rouletteCanvas.getWidth() / 2;
        double cy = rouletteCanvas.getHeight() / 2;

        gc.clearRect(0, 0, rouletteCanvas.getWidth(), rouletteCanvas.getHeight());

        // Outer cylinder body
        gc.setFill(Color.web("#2a2a4a"));
        gc.setStroke(Color.web("#e94560"));
        gc.setLineWidth(6.0); // Thickened stroke
        gc.fillOval(cx - RADIUS, cy - RADIUS, RADIUS * 2, RADIUS * 2);
        gc.strokeOval(cx - RADIUS, cy - RADIUS, RADIUS * 2, RADIUS * 2);

        // Pick a random golden bullet chamber just for the preview
        int bulletChamber = (int) (Math.random() * CHAMBERS);

        // Draw 6 chambers
        for (int i = 0; i < CHAMBERS; i++) {
            double angle = Math.toRadians(i * (360.0 / CHAMBERS));
            double chamberX = cx + Math.cos(angle) * (RADIUS * 0.60);
            double chamberY = cy + Math.sin(angle) * (RADIUS * 0.60);

            if (i == bulletChamber) {
                // Bullet chamber — glowing gold
                gc.setFill(Color.web("#f5a623"));
                gc.setStroke(Color.web("#ffcc44"));
            } else {
                // Empty chamber — dark hole
                gc.setFill(Color.web("#0d0d1a"));
                gc.setStroke(Color.web("#555588"));
            }

            gc.setLineWidth(3.0);
            gc.fillOval(chamberX - CHAMBER_RADIUS, chamberY - CHAMBER_RADIUS, CHAMBER_RADIUS * 2, CHAMBER_RADIUS * 2);
            gc.strokeOval(chamberX - CHAMBER_RADIUS, chamberY - CHAMBER_RADIUS, CHAMBER_RADIUS * 2, CHAMBER_RADIUS * 2);
        }

        // Center pin
        gc.setFill(Color.web("#e94560"));
        gc.fillOval(cx - 15, cy - 15, 30, 30);
    }
}