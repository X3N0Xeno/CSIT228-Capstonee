package com.capstone.javabox.csit228.games.hangman;

import javafx.animation.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class ScreenEffects {

    // Red flash overlay on wrong guess
    public static void flashRed(StackPane root) {
        Rectangle flash = new Rectangle();
        flash.widthProperty().bind(root.widthProperty());
        flash.heightProperty().bind(root.heightProperty());
        flash.setFill(Color.web("#e94560"));
        flash.setOpacity(0.35);
        flash.setMouseTransparent(true);

        root.getChildren().add(flash);

        FadeTransition ft = new FadeTransition(Duration.millis(400), flash);
        ft.setFromValue(0.35);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> root.getChildren().remove(flash));
        ft.play();
    }

    // Screen shake on death
    public static void shake(Stage stage) {
        double originalX = stage.getX();
        double originalY = stage.getY();

        Timeline shake = new Timeline(
                new KeyFrame(Duration.millis(0),   e -> stage.setX(originalX)),
                new KeyFrame(Duration.millis(50),  e -> stage.setX(originalX + 10)),
                new KeyFrame(Duration.millis(100), e -> stage.setX(originalX - 10)),
                new KeyFrame(Duration.millis(150), e -> stage.setX(originalX + 8)),
                new KeyFrame(Duration.millis(200), e -> stage.setX(originalX - 8)),
                new KeyFrame(Duration.millis(250), e -> stage.setX(originalX + 5)),
                new KeyFrame(Duration.millis(300), e -> stage.setX(originalX - 5)),
                new KeyFrame(Duration.millis(350), e -> stage.setX(originalX))
        );
        shake.play();
    }

    // Confetti on win
    public static void confetti(Pane root) {
        Random rand = new Random();
        String[] colors = {"#e94560", "#4caf50", "#f5a623", "#00bcd4", "#9c27b0", "#ffffff"};

        for (int i = 0; i < 60; i++) {
            Circle particle = new Circle(5, Color.web(colors[rand.nextInt(colors.length)]));
            double startX = rand.nextDouble() * root.getWidth();
            particle.setLayoutX(startX);
            particle.setLayoutY(-10);
            root.getChildren().add(particle);

            TranslateTransition fall = new TranslateTransition(
                    Duration.millis(1000 + rand.nextInt(1000)), particle);
            fall.setByY(root.getHeight() + 50);
            fall.setByX((rand.nextDouble() - 0.5) * 200);

            FadeTransition fade = new FadeTransition(Duration.millis(1500), particle);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            ParallelTransition pt = new ParallelTransition(fall, fade);
            pt.setDelay(Duration.millis(rand.nextInt(600)));
            pt.setOnFinished(e -> root.getChildren().remove(particle));
            pt.play();
        }
    }
}