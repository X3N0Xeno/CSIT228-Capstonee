package com.capstone.javabox.csit228.games.leaderboard;

import com.capstone.javabox.csit228.database.LeaderboardDAO;
import com.capstone.javabox.csit228.games.JavaboxAbstractController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class LeaderboardController extends JavaboxAbstractController {

    @FXML private VBox listKnucklebones;
    @FXML private VBox listSwarm;
    @FXML private VBox listWordle;
    @FXML private VBox listHangman;
    @FXML private VBox listUTTT;
    @FXML private VBox listFullHouse;

    @FXML
    public void initialize() {
        new Thread(() -> {
            List<LeaderboardDAO.Entry> kb = LeaderboardDAO.getKnucklebonesTop();
            List<LeaderboardDAO.Entry> swarm = LeaderboardDAO.getWindowSwarmTop();
            List<LeaderboardDAO.Entry> wordle = LeaderboardDAO.getWordleTop();
            List<LeaderboardDAO.Entry> hangman = LeaderboardDAO.getHangmanTop();
            List<LeaderboardDAO.Entry> uttt = LeaderboardDAO.getUTTTTop();
            List<LeaderboardDAO.Entry> fullHouse = LeaderboardDAO.getFullHouseTop();

            Platform.runLater(() -> {
                // Game | Data | Suffix | Font | Name Color | Score Color
                populateBoard(listKnucklebones, kb, "pts", "'JetBrains Mono', 'Courier New'", "#ffffff", "#5eafff");
                populateBoard(listSwarm, swarm, "pts", "'Courier New'", "#00FF41", "#00FF41");
                populateBoard(listFullHouse, fullHouse, "won", "'Georgia'", "#e0e0e0", "#d4af37");
                populateBoard(listWordle, wordle, "wins", "Monospace", "#eaeaea", "#4caf50");
                populateBoard(listHangman, hangman, "wins", "Monospace", "#eaeaea", "#e94560");
                populateBoard(listUTTT, uttt, "wins", "Monospace", "#eaeaea", "#f5a623");
            });
        }).start();
    }

    private void populateBoard(VBox container, List<LeaderboardDAO.Entry> data, String suffix, String font, String nameColor, String scoreColor) {
        container.getChildren().clear();

        if (data.isEmpty()) {
            Label empty = new Label("NO RECORDS FOUND.");
            empty.setStyle("-fx-text-fill: #555555; -fx-font-family: " + font + "; -fx-font-style: italic;");
            container.getChildren().add(empty);
            return;
        }

        int rank = 1;
        for (LeaderboardDAO.Entry entry : data) {
            HBox row = new HBox();

            Label nameLbl = new Label(rank + ". " + entry.username.toUpperCase());
            nameLbl.setStyle("-fx-text-fill: " + nameColor + "; -fx-font-family: " + font + "; -fx-font-size: 14px;");

            // Add a glow effect specifically for the hacker aesthetic of Window Swarm
            if (nameColor.equals("#00FF41")) {
                nameLbl.setEffect(new javafx.scene.effect.Glow(0.8));
            }

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label scoreLbl = new Label(entry.score + " " + suffix);
            scoreLbl.setStyle("-fx-text-fill: " + scoreColor + "; -fx-font-family: " + font + "; -fx-font-weight: bold; -fx-font-size: 14px;");

            if (scoreColor.equals("#00FF41")) {
                scoreLbl.setEffect(new javafx.scene.effect.Glow(0.8));
            }

            row.getChildren().addAll(nameLbl, spacer, scoreLbl);
            container.getChildren().add(row);
            rank++;
        }
    }

    @FXML
    private void handleBackToLobby() {
        quitToLobby();
    }
}