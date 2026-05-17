package com.capstone.javabox.csit228.games.previews;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class PreviewKnucklebonesController {

    @FXML private StackPane rootPane;
    @FXML private Rectangle scrollingBackground; // Injected as a Rectangle now

    @FXML
    public void initialize() {
        // 1. Clip the Root Pane (Prevents bounds bleed)
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(rootPane.widthProperty());
        clip.heightProperty().bind(rootPane.heightProperty());
        rootPane.setClip(clip);

        scrollingBackground.setManaged(false);
        // 2. Load the crossbones image
        Image logo = null;
        try {
            logo = new Image(getClass().getResourceAsStream("/com/capstone/javabox/csit228/images/crossbones_logo.jpg"));
        } catch (Exception e) {
            System.err.println("Preview Error: Could not find crossbones_logo.jpg!");
            return;
        }

        // 3. The FIX: Use ImagePattern on a physical shape
        // This paints the image as an infinitely repeating texture across the Rectangle
        double tileSize = 120.0;
        ImagePattern pattern = new ImagePattern(logo, 0, 0, tileSize, tileSize, false);
        scrollingBackground.setFill(pattern);

        // Force the absolute size of the shape
        scrollingBackground.setWidth(3000);
        scrollingBackground.setHeight(3000);

        // Shift it diagonally off-center so the screen starts deep inside the pattern
        scrollingBackground.setTranslateX(-500);
        scrollingBackground.setTranslateY(-500);

        // 4. The Seamless Diagonal Scroll Animation
        TranslateTransition scroll = new TranslateTransition(Duration.seconds(2.5), scrollingBackground);

        // Move from our shifted start point, exactly one tile width further
        scroll.setFromX(-500);
        scroll.setFromY(-500);
        scroll.setToX(-500 - tileSize);
        scroll.setToY(-500 - tileSize);

        scroll.setInterpolator(Interpolator.LINEAR);
        scroll.setCycleCount(Animation.INDEFINITE);
        scroll.play();
    }
}