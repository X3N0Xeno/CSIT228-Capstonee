package com.capstone.javabox.csit228.games;

import com.capstone.javabox.csit228.games.knucklebones.KnuckleBoneLauncher;
import com.capstone.javabox.csit228.games.windowswarm.WindowSwarmLauncher;
import com.capstone.javabox.csit228.utils.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class JavaboxController {
    @FXML
    private TilePane gameContainer;

    @FXML
    public void initialize() {
        //Hey! This is the game registry!
        List<JavaboxGame> availableGames = new ArrayList<>(List.of(
                //Add your game's Launcher here! Don't forget about the comma heh heh...
                //By the way, your launcher must implement JavaboxGame and your Controller must extend JavaboxAbstractController!
                new KnuckleBoneLauncher(),
                new WindowSwarmLauncher()
        ));

        for (JavaboxGame game : availableGames) {
            Button gameButton = new Button(game.getGameTitle());
            gameButton.setPrefSize(200, 200);
            gameButton.getStyleClass().add("game-tile");

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
            gameContainer.getChildren().add(gameButton);
        }
    }
}