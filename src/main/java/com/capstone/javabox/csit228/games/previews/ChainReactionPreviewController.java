package com.capstone.javabox.csit228.games.previews;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class ChainReactionPreviewController {

    @FXML private Pane animationCanvas;

    private static final int CELL_SIZE = 60;
    // Since our canvas is locked to 600x400, the exact center is 300, 200
    private static final double CX = 300;
    private static final double CY = 200;

    private final Color COLOR_BG = Color.web("#161a2b");
    private final Color COLOR_GRID = Color.web("#2a3253");
    private final Color COLOR_RED = Color.web("#ff4757");
    private final Color COLOR_BLUE = Color.web("#1e90ff");

    // UI Elements for animation
    private Rectangle centerCell, topCell, bottomCell, leftCell, rightCell;
    private List<Circle> centerDots = new ArrayList<>();
    private List<Circle> adjacentDots = new ArrayList<>();
    private List<Circle> primaryProjectiles = new ArrayList<>();
    private List<Circle> secondaryProjectiles = new ArrayList<>();

    @FXML
    public void initialize() {
        drawGrid();
        buildAnimationEntities();
        startChoreography();
    }

    private void drawGrid() {
        // Draw a 5x5 aesthetic grid behind everything
        int gridCols = 5;
        int gridRows = 5;
        double startX = CX - (gridCols * CELL_SIZE / 2.0);
        double startY = CY - (gridRows * CELL_SIZE / 2.0);

        for (int r = 0; r < gridRows; r++) {
            for (int c = 0; c < gridCols; c++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE, COLOR_BG);
                rect.setStroke(COLOR_GRID);
                rect.setX(startX + (c * CELL_SIZE));
                rect.setY(startY + (r * CELL_SIZE));
                rect.setArcWidth(10);
                rect.setArcHeight(10);
                animationCanvas.getChildren().add(rect);

                // Save references to the 5 active cells we will animate
                if (r == 2 && c == 2) centerCell = rect;
                if (r == 1 && c == 2) topCell = rect;
                if (r == 3 && c == 2) bottomCell = rect;
                if (r == 2 && c == 1) leftCell = rect;
                if (r == 2 && c == 3) rightCell = rect;
            }
        }
    }

    private void buildAnimationEntities() {
        // 1. Center Cell Dots (Starts with 3 Red)
        centerDots.add(createDot(CX, CY - 10, COLOR_RED));
        centerDots.add(createDot(CX - 12, CY + 10, COLOR_RED));
        centerDots.add(createDot(CX + 12, CY + 10, COLOR_RED));

        // The 4th dot that triggers the explosion
        Circle triggerDot = createDot(CX, CY, COLOR_RED);
        triggerDot.setScaleX(0); triggerDot.setScaleY(0); // Hidden initially
        centerDots.add(triggerDot);

        // 2. Adjacent Cells (Start with 3 Blue dots each)
        double[][] adjCenters = {
                {CX, CY - CELL_SIZE}, // Top
                {CX, CY + CELL_SIZE}, // Bottom
                {CX - CELL_SIZE, CY}, // Left
                {CX + CELL_SIZE, CY}  // Right
        };

        for (double[] pos : adjCenters) {
            adjacentDots.add(createDot(pos[0], pos[1] - 10, COLOR_BLUE));
            adjacentDots.add(createDot(pos[0] - 12, pos[1] + 10, COLOR_BLUE));
            adjacentDots.add(createDot(pos[0] + 12, pos[1] + 10, COLOR_BLUE));

            // The 4th dot (Red) that will appear upon impact
            Circle impactDot = createDot(pos[0], pos[1], COLOR_RED);
            impactDot.setScaleX(0); impactDot.setScaleY(0);
            adjacentDots.add(impactDot);
        }
    }

    private Circle createDot(double x, double y, Color color) {
        Circle dot = new Circle(x, y, 6, color);
        animationCanvas.getChildren().add(dot);
        return dot;
    }

    private void startChoreography() {
        SequentialTransition loop = new SequentialTransition();

        // ---------------------------------------------------------
        // PHASE 1: RESET STATE (In case of loop)
        // ---------------------------------------------------------
        ParallelTransition resetAnim = new ParallelTransition();
        resetAnim.setOnFinished(e -> {
            centerCell.setStroke(COLOR_RED);
            topCell.setStroke(COLOR_BLUE); bottomCell.setStroke(COLOR_BLUE);
            leftCell.setStroke(COLOR_BLUE); rightCell.setStroke(COLOR_BLUE);

            // Reset center dots
            for (int i = 0; i < 3; i++) {
                centerDots.get(i).setOpacity(1); centerDots.get(i).setFill(COLOR_RED);
            }
            centerDots.get(3).setScaleX(0); centerDots.get(3).setScaleY(0); centerDots.get(3).setOpacity(1);

            // Reset adjacent dots
            for (int i = 0; i < adjacentDots.size(); i++) {
                Circle dot = adjacentDots.get(i);
                dot.setOpacity(1);
                if (i % 4 == 3) { // The 4th trigger dot
                    dot.setScaleX(0); dot.setScaleY(0); dot.setFill(COLOR_RED);
                } else {
                    dot.setScaleX(1); dot.setScaleY(1); dot.setFill(COLOR_BLUE);
                }
            }

            // Purge old projectiles
            animationCanvas.getChildren().removeAll(primaryProjectiles);
            animationCanvas.getChildren().removeAll(secondaryProjectiles);
            primaryProjectiles.clear();
            secondaryProjectiles.clear();
        });
        loop.getChildren().addAll(resetAnim, new PauseTransition(Duration.millis(800)));

        // ---------------------------------------------------------
        // PHASE 2: CRITICAL MASS
        // ---------------------------------------------------------
        ScaleTransition criticalMass = new ScaleTransition(Duration.millis(300), centerDots.get(3));
        criticalMass.setToX(1); criticalMass.setToY(1);
        loop.getChildren().addAll(criticalMass, new PauseTransition(Duration.millis(300)));

        // ---------------------------------------------------------
        // PHASE 3: FIRST EXPLOSION (Center -> Adjacents)
        // ---------------------------------------------------------
        ParallelTransition firstExplosion = new ParallelTransition();
        firstExplosion.setOnFinished(e -> {
            centerCell.setStroke(COLOR_GRID);
            for (Circle c : centerDots) c.setOpacity(0); // Vanish

            // Spawn 4 projectiles
            int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
            for (int[] d : dirs) {
                Circle proj = createDot(CX, CY, COLOR_RED);
                primaryProjectiles.add(proj);

                TranslateTransition move = new TranslateTransition(Duration.millis(250), proj);
                move.setByX(d[0] * CELL_SIZE);
                move.setByY(d[1] * CELL_SIZE);
                move.play();
            }
        });
        loop.getChildren().addAll(firstExplosion, new PauseTransition(Duration.millis(250)));

        // ---------------------------------------------------------
        // PHASE 4: IMPACT & CONVERSION
        // ---------------------------------------------------------
        ParallelTransition impact = new ParallelTransition();
        impact.setOnFinished(e -> {
            for (Circle p : primaryProjectiles) p.setOpacity(0); // Hide projectiles

            // Convert adjacent borders
            topCell.setStroke(COLOR_RED); bottomCell.setStroke(COLOR_RED);
            leftCell.setStroke(COLOR_RED); rightCell.setStroke(COLOR_RED);

            // Convert adjacent dots and pop in the 4th dot
            for (int i = 0; i < adjacentDots.size(); i++) {
                Circle dot = adjacentDots.get(i);
                if (i % 4 == 3) {
                    ScaleTransition pop = new ScaleTransition(Duration.millis(150), dot);
                    pop.setToX(1); pop.setToY(1);
                    pop.play();
                } else {
                    dot.setFill(COLOR_RED);
                }
            }
        });
        loop.getChildren().addAll(impact, new PauseTransition(Duration.millis(300)));

        // ---------------------------------------------------------
        // PHASE 5: SECONDARY CHAIN REACTION
        // ---------------------------------------------------------
        ParallelTransition secondaryExplosion = new ParallelTransition();
        secondaryExplosion.setOnFinished(e -> {
            topCell.setStroke(COLOR_GRID); bottomCell.setStroke(COLOR_GRID);
            leftCell.setStroke(COLOR_GRID); rightCell.setStroke(COLOR_GRID);
            for (Circle c : adjacentDots) c.setOpacity(0); // Vanish

            double[][] starts = {
                    {CX, CY - CELL_SIZE}, {CX, CY + CELL_SIZE},
                    {CX - CELL_SIZE, CY}, {CX + CELL_SIZE, CY}
            };
            int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

            for (double[] pos : starts) {
                for (int[] d : dirs) {
                    Circle proj = createDot(pos[0], pos[1], COLOR_RED);
                    secondaryProjectiles.add(proj);

                    TranslateTransition move = new TranslateTransition(Duration.millis(300), proj);
                    move.setByX(d[0] * CELL_SIZE);
                    move.setByY(d[1] * CELL_SIZE);

                    FadeTransition fade = new FadeTransition(Duration.millis(300), proj);
                    fade.setToValue(0);

                    move.play(); fade.play();
                }
            }
        });
        loop.getChildren().addAll(secondaryExplosion, new PauseTransition(Duration.millis(1200)));

        // Run infinitely
        loop.setOnFinished(e -> loop.play());
        loop.play();
    }
}