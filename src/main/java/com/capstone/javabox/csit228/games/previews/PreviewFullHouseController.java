package com.capstone.javabox.csit228.games.previews;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Random;

public class PreviewFullHouseController {

    @FXML
    private VBox titleBox;
    @FXML
    private Pane racerPane;

    private final String[] SPRITES = {
            "shelby_sprite.png", "shamrock_sprite.png", "garrison_sprite.png",
            "cottage_sprite.png", "keep_sprite.png", "pub_sprite.png",
            "mill_sprite.png", "parlor_sprite.png", "alley_sprite.png",
            "wail_sprite.png", "vault_sprite.png", "loot_sprite.png", "spire_sprite.png"
    };

    private final Random rand = new Random();
    private final int RACER_COUNT = 10; // Slightly reduced to lessen the chaos

    @FXML
    public void initialize() {
        racerPane.setMouseTransparent(true);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(racerPane.widthProperty());
        clip.heightProperty().bind(racerPane.heightProperty());
        racerPane.setClip(clip);

        TranslateTransition bob = new TranslateTransition(Duration.seconds(1.5), titleBox);
        bob.setByY(-15.0);
        bob.setCycleCount(Animation.INDEFINITE);
        bob.setAutoReverse(true);
        bob.setInterpolator(Interpolator.EASE_BOTH);
        bob.play();

        // 1. FIX: Spawn Groups instead of just ImageViews
        for (int i = 0; i < RACER_COUNT; i++) {
            Group racerGroup = new Group();

            ImageView trail2 = new ImageView();
            trail2.setOpacity(0.15); // Faintest

            ImageView trail1 = new ImageView();
            trail1.setOpacity(0.4); // Medium

            ImageView mainImage = new ImageView();

            racerGroup.getChildren().addAll(trail2, trail1, mainImage);
            racerPane.getChildren().add(racerGroup);

            launchRacer(racerGroup, true);
        }
    }

    /**
     * Updated to accept a Group containing the 3 image layers.
     */
    private void launchRacer(Group racerGroup, boolean isInitialSpawn) {
        String spriteName = SPRITES[rand.nextInt(SPRITES.length)];
        try {
            Image sprite = new Image(getClass().getResourceAsStream("/com/capstone/javabox/csit228/images/" + spriteName));

            // Apply the image and size to all 3 layers inside the group
            for (int i = 0; i < 3; i++) {
                ImageView imgView = (ImageView) racerGroup.getChildren().get(i);
                imgView.setImage(sprite);
                imgView.setFitWidth(65);
                imgView.setFitHeight(65);
                imgView.setPreserveRatio(true);
            }
        } catch (Exception e) {
            System.err.println("Preview Error: Missing sprite " + spriteName);
        }

        // Random Y Position
        double randomY = 50 + (rand.nextDouble() * 600);
        racerGroup.setLayoutY(randomY);

        // Speed & Physics Math
        double durationSeconds = 1.5 + (rand.nextDouble() * 3.5);
        double speedFactor = 5.0 - durationSeconds; // Yields 0.0 (Slow) to 3.5 (Fast)

        double tiltAngle = 5.0 + (speedFactor * 5.0);
        racerGroup.setRotate(tiltAngle);

        // --- NEW: Dynamic Dash Trail Math ---
        // If they are fast, the gap is wide (up to 30px). If slow, they clump together.
        double trailGap = speedFactor * 8.5;

        // Push the ghosts backwards on the X axis
        racerGroup.getChildren().get(0).setLayoutX(-trailGap * 2); // Trail 2
        racerGroup.getChildren().get(1).setLayoutX(-trailGap);     // Trail 1
        racerGroup.getChildren().get(2).setLayoutX(0);             // Main Image

        // Snap the entire group off-screen safely
        racerGroup.setTranslateX(-150);

        // The Traverse Animation
        TranslateTransition traverse = new TranslateTransition(Duration.seconds(durationSeconds), racerGroup);
        traverse.setFromX(-150);
        traverse.setToX(1400);
        traverse.setInterpolator(Interpolator.LINEAR);

        if (isInitialSpawn) {
            traverse.setDelay(Duration.seconds(rand.nextDouble() * 4.0));
        } else {
            traverse.setDelay(Duration.seconds(rand.nextDouble() * 3.0));
        }

        traverse.setOnFinished(e -> launchRacer(racerGroup, false));
        traverse.play();
    }
}