package com.capstone.javabox.csit228.games;

import com.capstone.javabox.csit228.games.fullhouse.FullHouseLauncher;
import com.capstone.javabox.csit228.games.hangman.HangmanLauncher;
import com.capstone.javabox.csit228.games.knucklebones.KnuckleBoneLauncher;
import com.capstone.javabox.csit228.games.leaderboard.LeaderboardLauncher;
import com.capstone.javabox.csit228.games.ultimatettt.UltimateTTTLauncher;
import com.capstone.javabox.csit228.games.windowswarm.WindowSwarmLauncher;
import com.capstone.javabox.csit228.games.wordle.WordleLauncher;
import com.capstone.javabox.csit228.utils.SoundManager;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class JavaboxController {
    @FXML private VBox gameContainer;
    @FXML private StackPane previewArea;

    private Node currentlyVisibleNode;
    private Node defaultView;

    @FXML
    public void initialize() {
        //Hey! This is the game registry!
        List<JavaboxGame> availableGames = new ArrayList<>(List.of(
                //Add your game's Launcher here! Don't forget about the comma heh heh...
                //By the way, your launcher must implement JavaboxGame and your Controller must extend JavaboxAbstractController!
                new KnuckleBoneLauncher(),
                new WindowSwarmLauncher(),
                new HangmanLauncher(),
                new WordleLauncher(),
                new UltimateTTTLauncher(),
                new FullHouseLauncher(),
                new LeaderboardLauncher()
        ));

        //Load the Default Background
        try {
            defaultView = FXMLLoader.load(getClass().getResource("preview/preview-default.fxml"));
            previewArea.getChildren().add(defaultView);
            currentlyVisibleNode = defaultView;
        } catch (Exception e) {
            System.err.println("Could not load default preview.");
        }

        //Generate buttons and link them to their specific preview files
        for (JavaboxGame game : availableGames) {
            Button gameButton = new Button(game.getGameTitle().toUpperCase());
            gameButton.getStyleClass().add("lobby-button");
            gameButton.setMaxWidth(Double.MAX_VALUE);

            String previewFxmlFile = "preview/preview-default.fxml";
            String hoverSound = "sfx_button.mp3";

            //Sound Effects for each new button
            if (game instanceof KnuckleBoneLauncher) {
                previewFxmlFile = "preview/preview-knucklebones.fxml";
                hoverSound = "sfx_dice.mp3";
            }
            else if (game instanceof WindowSwarmLauncher) {
                previewFxmlFile = "preview/preview-windowswarm.fxml";
                hoverSound = "sfx_window_swarm_hover.mp3";
            }
            else if(game instanceof HangmanLauncher){
                previewFxmlFile = "preview/preview-hangman.fxml";
                hoverSound = "sfx_reload.mp3";
            }
            else if(game instanceof WordleLauncher){
                previewFxmlFile = "preview/preview-wordle.fxml";
                hoverSound = "sfx_ui_prompt.mp3";
            }
            else if(game instanceof UltimateTTTLauncher){
                previewFxmlFile = "preview/preview-wordle.fxml";
                hoverSound = "sfx_ui_prompt.mp3";
            }
            else if(game instanceof FullHouseLauncher){
                previewFxmlFile = "preview/preview-fullhouse.fxml";
                hoverSound = "sfx_fanfare.mp3";
            }

            try {
                Node previewNode = FXMLLoader.load(getClass().getResource(previewFxmlFile));
                previewNode.setOpacity(0);
                previewArea.getChildren().add(previewNode);

                gameButton.setOnMouseEntered(e -> crossfadeTo(previewNode));
            } catch (Exception e) {
                System.err.println("Could not load preview file: " + previewFxmlFile);
            }

            applyHoverJuice(gameButton, hoverSound);

            gameContainer.getChildren().add(gameButton);


            gameButton.setOnAction(event -> {
                SoundManager.playSFX("sfx_button.mp3");
                Stage currentStage = (Stage) gameButton.getScene().getWindow();
                Scene lobbyScene = gameButton.getScene();
                Runnable returnToLobby = () -> {
                    currentStage.setScene(lobbyScene);
                    currentStage.setTitle("Welcome to Javabox");
                    currentStage.centerOnScreen();
                };
                game.launchGame(currentStage, returnToLobby);
            });
        }
        gameContainer.setOnMouseExited(e -> crossfadeTo(defaultView));
    }

    private void crossfadeTo(Node targetNode) {
        if (targetNode == null || targetNode == currentlyVisibleNode) return;

        if (currentlyVisibleNode != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentlyVisibleNode);
            fadeOut.setToValue(0);
            fadeOut.play();
        }

        targetNode.toFront();
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), targetNode);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        currentlyVisibleNode = targetNode;
    }

    private void applyHoverJuice(Button button, String hoverSoundFile) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), button);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), button);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        Timeline jiggle = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(button.rotateProperty(), 0)),
                new KeyFrame(Duration.millis(50), new KeyValue(button.rotateProperty(), -2)),
                new KeyFrame(Duration.millis(100), new KeyValue(button.rotateProperty(), 2)),
                new KeyFrame(Duration.millis(150), new KeyValue(button.rotateProperty(), 0))
        );

        button.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, e -> {
            if (hoverSoundFile != null && !hoverSoundFile.isEmpty()) {
                SoundManager.playSFX(hoverSoundFile);
            }

            scaleUp.playFromStart();
            jiggle.playFromStart();
        });

        button.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, e -> {
            scaleUp.stop();
            jiggle.stop();
            button.setRotate(0);
            scaleDown.playFromStart();
        });
    }
}