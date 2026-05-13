package com.capstone.javabox.csit228.games.hangman;

import javafx.animation.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class RouletteCanvas extends Canvas {

    private static final int CHAMBERS = 6;
    private static final double RADIUS = 60;
    private static final double CHAMBER_RADIUS = 12;

    private int bulletChamber;
    private int currentAngleOffset = 0;

    public RouletteCanvas() {
        super(160, 160);
        randomizeBullet();
        draw();
    }

    public void randomizeBullet() {
        bulletChamber = (int) (Math.random() * CHAMBERS);
    }

    // Returns true if the spin lands on the bullet
    public boolean spin(Runnable onLandOnBullet, Runnable onSurvive) {
        int targetChamber = (int) (Math.random() * CHAMBERS);
        boolean hitBullet = (targetChamber == bulletChamber);

        // How many degrees to spin: multiple full rotations + land on targetChamber
        double degreesPerChamber = 360.0 / CHAMBERS;
        double extraSpins = 360 * 3; // 3 full spins for drama
        double targetDegrees = currentAngleOffset + extraSpins + (targetChamber * degreesPerChamber);

        RotateTransition rt = new RotateTransition(Duration.millis(1200), this);
        rt.setFromAngle(currentAngleOffset);
        rt.setToAngle(targetDegrees);
        rt.setInterpolator(Interpolator.EASE_OUT);
        rt.setOnFinished(e -> {
            currentAngleOffset = (int)(targetDegrees % 360);
            if (hitBullet) onLandOnBullet.run();
            else onSurvive.run();
        });
        rt.play();

        return hitBullet;
    }

    public void reset() {
        currentAngleOffset = 0;
        randomizeBullet();
        setRotate(0);
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double cx = getWidth() / 2;
        double cy = getHeight() / 2;

        // Clear
        gc.clearRect(0, 0, getWidth(), getHeight());

        // Outer cylinder body
        gc.setFill(Color.web("#2a2a4a"));
        gc.setStroke(Color.web("#e94560"));
        gc.setLineWidth(2.5);
        gc.fillOval(cx - RADIUS, cy - RADIUS, RADIUS * 2, RADIUS * 2);
        gc.strokeOval(cx - RADIUS, cy - RADIUS, RADIUS * 2, RADIUS * 2);

        // Draw 6 chambers
        for (int i = 0; i < CHAMBERS; i++) {
            double angle = Math.toRadians(i * (360.0 / CHAMBERS));
            double chamberX = cx + Math.cos(angle) * (RADIUS * 0.55);
            double chamberY = cy + Math.sin(angle) * (RADIUS * 0.55);

            if (i == bulletChamber) {
                // Bullet chamber — glowing gold
                gc.setFill(Color.web("#f5a623"));
                gc.setStroke(Color.web("#ffcc44"));
            } else {
                // Empty chamber — dark hole
                gc.setFill(Color.web("#0d0d1a"));
                gc.setStroke(Color.web("#555588"));
            }
            gc.setLineWidth(1.5);
            gc.fillOval(chamberX - CHAMBER_RADIUS, chamberY - CHAMBER_RADIUS,
                    CHAMBER_RADIUS * 2, CHAMBER_RADIUS * 2);
            gc.strokeOval(chamberX - CHAMBER_RADIUS, chamberY - CHAMBER_RADIUS,
                    CHAMBER_RADIUS * 2, CHAMBER_RADIUS * 2);
        }

        // Center pin
        gc.setFill(Color.web("#e94560"));
        gc.fillOval(cx - 5, cy - 5, 10, 10);
    }
}