package com.capstone.javabox.csit228.games.knucklebones;

import com.capstone.javabox.csit228.games.JavaboxAbstractController;
import com.capstone.javabox.csit228.utils.JavaboxUtils;
import com.capstone.javabox.csit228.utils.SoundManager;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class KnuckleBoneMenuController extends JavaboxAbstractController {
    @FXML private HBox titleBox;
    @FXML private VBox menuBox;
    @FXML private VBox helpBox;
    @FXML private VBox lobbyBox;

    @FXML private TextField p1Name;
    @FXML private TextField p2Name;
    @FXML private ToggleGroup p1Color;
    @FXML private ToggleGroup p2Color;

    @FXML private Label player1;
    @FXML private Label player2;

    public void initialize() {
        SoundManager.playMusic("music_COTL_knucklebones.mp3");
        player1.setStyle("-fx-text-fill: white;");
        player2.setStyle("-fx-text-fill: white;");
        double delay = 0;

        for (Node node : titleBox.getChildren()) {
            if (node instanceof Label) {
                TranslateTransition bounce = new TranslateTransition(Duration.seconds(0.6), node);
                bounce.setByY(-15);
                bounce.setCycleCount(TranslateTransition.INDEFINITE);
                bounce.setAutoReverse(true);
                bounce.setInterpolator(Interpolator.SPLINE(0.5, 0.1, 0.5, 0.9));

                bounce.setDelay(Duration.seconds(delay));
                delay += 0.1;

                bounce.play();
            }
        }
        menuBox.setVisible(true);
        helpBox.setVisible(false);
        lobbyBox.setVisible(false);
    }

    @FXML
    public void onPlayButtonClicked(ActionEvent event) {
        menuBox.setVisible(false);
        lobbyBox.setVisible(true);
    }

    @FXML
    public void onHelpButtonClicked(ActionEvent event){
        menuBox.setVisible(false);
        helpBox.setVisible(true);
    }

    @FXML
    public void onExitButtonClicked(ActionEvent event){
        if (JavaboxUtils.showConfirmation("Exit Game", "Are you sure you want to quit to the lobby?")) {
            SoundManager.stopMusic();
            quitToLobby();
        }
    }

    @FXML
    public void onBackToMenuClicked(ActionEvent event){
        menuBox.setVisible(true);
        helpBox.setVisible(false);
        lobbyBox.setVisible(false);
    }

    @FXML
    public void onStartRoundButtonClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("knucklebone-view.fxml"));
            Scene gameScene = new Scene(loader.load(), 1280, 720);

            KnuckleBoneGameController gameController = getKnuckleBoneGameController(loader);


            gameController.setQuitCallback(this.quitCallback);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Knucklebones - Match Start!");
            stage.setScene(gameScene);

        } catch (IOException e) {
            e.printStackTrace();
            JavaboxUtils.showAlert(
                    Alert.AlertType.ERROR,
                    "System Error",
                    "Failed to load game screen",
                    "Check if knucklebone-view.fxml exists."
            );
        }
    }

    private KnuckleBoneGameController getKnuckleBoneGameController(FXMLLoader loader) {
        KnuckleBoneGameController gameController = loader.getController();

        //Player Data
        String name1 = p1Name.getText().trim().isEmpty() ? "Player 1" : p1Name.getText();
        String name2 = p2Name.getText().trim().isEmpty() ? "Player 2" : p2Name.getText();

        ToggleButton selected1 = (ToggleButton) p1Color.getSelectedToggle();
        ToggleButton selected2 = (ToggleButton) p2Color.getSelectedToggle();
        String color1 = (selected1 != null) ? selected1.getStyle() : "-fx-background-color: #ffffff;";
        String color2 = (selected2 != null) ? selected2.getStyle() : "-fx-background-color: #ffffff;";

        gameController.setPlayerData(name1, color1, name2, color2);
        return gameController;
    }

    @FXML
    public void onToggleButtonClicked() {
        ToggleButton selected1 = (ToggleButton) p1Color.getSelectedToggle();
        ToggleButton selected2 = (ToggleButton) p2Color.getSelectedToggle();

        String color1 = (selected1 != null) ? selected1.getStyle() : "-fx-background-color: #ffffff;";
        String color2 = (selected2 != null) ? selected2.getStyle() : "-fx-background-color: #ffffff;";
        String hex1 = JavaboxUtils.extractHex(color1);
        String hex2 = JavaboxUtils.extractHex(color2);

        // Inject the color specifically into the text-fill property
        player1.setStyle("-fx-text-fill: " + hex1 + ";");
        player2.setStyle("-fx-text-fill: " + hex2 + ";");
    }
}