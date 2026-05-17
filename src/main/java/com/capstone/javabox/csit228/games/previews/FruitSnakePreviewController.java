package com.capstone.javabox.csit228.games.previews;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class FruitSnakePreviewController {

    @FXML private Pane animationCanvas;

    private Circle apple;
    private List<Rectangle> snakeBody = new ArrayList<>();

    // Tracks how far along the invisible track the animation is
    private double currentDistance = 0;
    private final double SPEED = 4.5; // Adjust this to make the chase faster or slower

    @FXML
    public void initialize() {
        spawnEntities();
        startChaseLoop();
    }

    private void spawnEntities() {
        // 1. Create the Apple (Player)
        apple = new Circle(14, Color.web("#6ab04c"));

        // FIX: Force the Circle's origin to match the Rectangle's top-left origin
        apple.setCenterX(14);
        apple.setCenterY(14);

        apple.setStroke(Color.web("#badc58"));
        apple.setStrokeWidth(2);
        animationCanvas.getChildren().add(apple);

        // 2. Create the Snake (Red Squares)
        int snakeLength = 6;
        for (int i = 0; i < snakeLength; i++) {
            Rectangle segment = new Rectangle(28, 28);
            segment.setArcWidth(8);
            segment.setArcHeight(8);

            // Make the head slightly brighter, the body darker
            if (i == 0) {
                segment.setFill(Color.web("#ff7979"));
            } else {
                segment.setFill(Color.web("#eb4d4b"));
            }

            snakeBody.add(segment);
            animationCanvas.getChildren().add(segment);
        }
    }

    private void startChaseLoop() {
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double w = animationCanvas.getWidth();
                double h = animationCanvas.getHeight();

                // Wait until JavaFX has fully calculated the window size
                if (w == 0 || h == 0) return;

                // Define the invisible track (A box surrounding the text)
                double trackWidth = 550;
                double trackHeight = 280;
                double startX = (w - trackWidth) / 2;
                double startY = (h - trackHeight) / 2;
                double perimeter = 2 * trackWidth + 2 * trackHeight;

                // Move forward along the track
                currentDistance = (currentDistance + SPEED) % perimeter;

                // Place the apple at the front
                updatePosition(apple, currentDistance, startX, startY, trackWidth, trackHeight, 14);

                // Place the snake segments trailing behind
                for (int i = 0; i < snakeBody.size(); i++) {
                    // Head is 100px behind the apple, each body part is 32px behind the previous one
                    double offset = 100 + (i * 32);
                    double segmentDist = (currentDistance - offset + perimeter) % perimeter;

                    updatePosition(snakeBody.get(i), segmentDist, startX, startY, trackWidth, trackHeight, 14);
                }
            }
        };
        loop.start();
    }

    /**
     * Maps a 1D distance value to a 2D (X, Y) coordinate on a rectangle perimeter.
     */
    private void updatePosition(Node node, double distance, double x, double y, double w, double h, double centerOffset) {
        double px, py;

        if (distance < w) {
            // Top Edge (Moving Right)
            px = x + distance;
            py = y;
        } else if (distance < w + h) {
            // Right Edge (Moving Down)
            px = x + w;
            py = y + (distance - w);
        } else if (distance < 2 * w + h) {
            // Bottom Edge (Moving Left)
            px = x + w - (distance - (w + h));
            py = y + h;
        } else {
            // Left Edge (Moving Up)
            px = x;
            py = y + h - (distance - (2 * w + h));
        }

        // Apply translation, shifting by the center offset so the objects perfectly straddle the invisible line
        node.setTranslateX(px - centerOffset);
        node.setTranslateY(py - centerOffset);
    }
}