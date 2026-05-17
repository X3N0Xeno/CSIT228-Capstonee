package com.capstone.javabox.csit228.games.bookwormbattle;

import com.capstone.javabox.csit228.games.JavaboxAbstractController;
import com.capstone.javabox.csit228.utils.SoundManager;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class BookwormBattleMenuController extends JavaboxAbstractController {

    @FXML private VBox mainMenuVBox;
    @FXML private HBox titleContainer;

    @FXML private TextField p1NameInput;
    @FXML private TextField p2NameInput;

    @FXML
    public void initialize() {
        SoundManager.playMusic("music_bookworm_menu.mp3");
        createBouncingTitle("BOOKWORM BATTLE");
    }

    private void createBouncingTitle(String text) {
        titleContainer.getChildren().clear();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Label letter = new Label(String.valueOf(c));
            letter.setTextFill(Color.web("#ffd32a"));
            letter.setFont(Font.font("System", FontWeight.BOLD, 80));
            if (c == ' ') letter.setMinWidth(30);
            titleContainer.getChildren().add(letter);

            TranslateTransition bounce = new TranslateTransition(Duration.seconds(0.6), letter);
            bounce.setByY(-30);
            bounce.setCycleCount(TranslateTransition.INDEFINITE);
            bounce.setAutoReverse(true);
            bounce.setDelay(Duration.millis(i * 100));
            bounce.play();
        }
    }

    @FXML
    private void onPlayGame() {
        SoundManager.playSFX("sfx_buttonclick.wav");

        // Grab the aliases (default to Player 1 / Player 2 if left blank)
        String p1Alias = (p1NameInput != null && !p1NameInput.getText().trim().isEmpty()) ? p1NameInput.getText().trim() : "Player 1";
        String p2Alias = (p2NameInput != null && !p2NameInput.getText().trim().isEmpty()) ? p2NameInput.getText().trim() : "Player 2";

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bookwormbattle-view.fxml"));
            Parent battleRoot = loader.load();
            BookwormBattleController gameController = loader.getController();
            gameController.setQuitCallback(this.quitCallback);

            Stage currentStage = (Stage) mainMenuVBox.getScene().getWindow();
            currentStage.getScene().setRoot(battleRoot);

            // Pass the names into the game!
            gameController.startCountdown(p1Alias, p2Alias);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onHowToPlay() {
        SoundManager.playSFX("sfx_buttonclick.wav");
        Stage infoStage = new Stage();
        infoStage.initModality(Modality.APPLICATION_MODAL);
        infoStage.setTitle("Bookworm Battle - Official Manual");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #1e272e;");

        String content =
                "--- THE BASICS ---\n" +
                        "• Spell words (3+ letters) to damage the enemy.\n" +
                        "• Letters are cleared and replaced by tiles above (Gravity).\n" +
                        "• Spell a 5+ letter word to SEED a Gem on your board.\n" +
                        "• Use that Gem in a future word to HARVEST its effect.\n\n" +
                        "--- GEMS ---\n" +
                        "• 5L (Purple): Poison (1 dmg/turn for 2 turns).\n" +
                        "• 6L (Green): Heal (+2.5 Hearts).\n" +
                        "• 7L (Orange): Power Down (Halve enemy next attack).\n" +
                        "• 8L+ (Blue): Freeze (Skip enemy's next turn).\n\n" +
                        "--- POTIONS (Dropped every 2 phases) ---\n" +
                        "• HEALING: Restores 3 Hearts instantly.\n" +
                        "• STRENGTH: Doubles the damage of your next word.\n" +
                        "• PURIFY: Removes Poison, Weakness, and Freeze.\n\n" +
                        "--- CARDS (From Reward Wheel) ---\n" +
                        "• SCRAMBLE COL/ROW: Click enemy tile to ruin that line with rare letters (J, K, X, Y, Q, Z).\n" +
                        "• LOCKDOWN: Click an enemy tile to lock it from being used.\n" +
                        "• GEM STEAL: Click an enemy gem to take it for your board.\n" +
                        "• GEM NUKE: Instantly destroys all gems on the enemy board.\n" +
                        "• HEALING: Restores 5 Hearts.\n" +
                        "• SHIELD: Immune to the next instance of damage.\n" +
                        "• SHOP DELUXE: Receive one of each Potion.\n" +
                        "• HELP ME: Gain +1 Scramble charge.";

        Label label = new Label(content);
        label.setStyle("-fx-text-fill: white; -fx-font-family: 'Consolas'; -fx-font-size: 13;");
        label.setWrapText(true);

        ScrollPane scrollPane = new ScrollPane(label);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setStyle("-fx-background: #2f3542; -fx-background-color: #2f3542;");

        Button closeBtn = new Button("UNDERSTOOD");
        closeBtn.setStyle("-fx-background-color: #05c46b; -fx-text-fill: white; -fx-font-weight: bold;");
        closeBtn.setOnAction(e -> infoStage.close());

        layout.getChildren().addAll(scrollPane, closeBtn);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 450, 600);
        infoStage.setScene(scene);
        infoStage.show();
    }

    @FXML
    private void onExit() {
        SoundManager.playSFX("sfx_buttonclick.wav");
        SoundManager.stopMusic();
        quitToLobby();
    }
}