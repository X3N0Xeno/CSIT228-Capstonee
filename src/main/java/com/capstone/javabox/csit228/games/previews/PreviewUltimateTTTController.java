package com.capstone.javabox.csit228.games.previews;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class PreviewUltimateTTTController {

    @FXML private Label let0, let1, let2, let3, let4, let5, let6, let7, let8;

    @FXML
    public void initialize() {
        Label[] letters = {let0, let1, let2, let3, let4, let5, let6, let7, let8};

        // Create the master loop
        Timeline boardLoop = new Timeline();

        // Step 1: Pop each letter in sequentially
        for (int i = 0; i < letters.length; i++) {
            final int index = i;

            // Add a keyframe that triggers at (0.3s * index)
            KeyFrame popIn = new KeyFrame(Duration.seconds(0.3 * index), e -> {
                ScaleTransition st = new ScaleTransition(Duration.seconds(0.25), letters[index]);
                st.setToX(1.0);
                st.setToY(1.0);

                // Add a little springy bounce effect!
                st.setFromX(1.5);
                st.setFromY(1.5);
                st.play();
            });
            boardLoop.getKeyFrames().add(popIn);
        }

        // Step 2: Clear the board after they are all drawn
        // The last letter spawns at 2.4s. We wait until 4.5s to wipe the board.
        KeyFrame wipeBoard = new KeyFrame(Duration.seconds(4.5), e -> {
            for (Label letter : letters) {
                ScaleTransition st = new ScaleTransition(Duration.seconds(0.2), letter);
                st.setToX(0.0);
                st.setToY(0.0);
                st.play();
            }
        });
        boardLoop.getKeyFrames().add(wipeBoard);

        // Step 3: Add a blank frame at 5.0s to define the total length of the loop
        boardLoop.getKeyFrames().add(new KeyFrame(Duration.seconds(5.0)));

        boardLoop.setCycleCount(Animation.INDEFINITE);
        boardLoop.play();
    }
}