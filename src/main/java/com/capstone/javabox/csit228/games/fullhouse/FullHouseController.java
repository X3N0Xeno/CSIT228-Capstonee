package com.capstone.javabox.csit228.games.fullhouse;

import com.capstone.javabox.csit228.games.JavaboxAbstractController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.util.Arrays;
import java.util.List;

public class FullHouseController extends JavaboxAbstractController {

    // --- SETUP UI ---
    @FXML private VBox setupPane;
    @FXML private HBox cardContainer;
    @FXML private TextField txtPlayerName;
    @FXML private TextArea txtBets;
    @FXML private Button btnStartRaceMenu;

    // --- RACE UI ---
    @FXML private VBox racePane;
    @FXML private Rectangle nH1, nH2, nH3, nH4, nH5;
    @FXML private ProgressBar sH1, sH2, sH3, sH4, sH5;
    @FXML private Label lH1, lH2, lH3, lH4, lH5;
    @FXML private Label qH1, qH2, qH3, qH4, qH5;
    @FXML private Label lblStatus;
    @FXML private Button btnExit, btnNextRace, btnSpeed;

    private List<House> houses;
    private List<Rectangle> nodes;
    private List<ProgressBar> staminas;
    private List<Label> quotes;

    // Mathematically perfect finish line based on layout:
    // Line is at 960px. House starts at 120px and is 45px wide.
    // 960 - 120 - 45 = 795 distance to travel to perfectly touch the line.
    private final double FINISH_LINE = 795.0;
    private int totalBets = 0;
    private House selectedHouse = null;
    private volatile boolean isRaceOver = false;
    private int speedMultiplier = 1; // Used for the speed toggle

    @FXML
    public void initialize() {
        houses = Arrays.asList(
                new House("The Shelby Estate", "By order of the hopping blinders.", "shelby.png", 16, 70, 7),
                new House("Shamrock Manor", "Gold in the basement, speed in the bricks.", "shamrock.png", 12, 120, 9),
                new House("The Rusty Garrison", "Smells like stout and victory.", "garrison.png", 14, 90, 5),
                new House("Gilded Cottage", "Lavish, small, and suspiciously fast.", "cottage.png", 18, 45, 4),
                new House("Cobblestone Keep", "Slow, steady, and heavily rigged.", "keep.png", 10, 160, 8)
        );

        nodes = Arrays.asList(nH1, nH2, nH3, nH4, nH5);
        staminas = Arrays.asList(sH1, sH2, sH3, sH4, sH5);
        quotes = Arrays.asList(qH1, qH2, qH3, qH4, qH5);
        List<Label> labels = Arrays.asList(lH1, lH2, lH3, lH4, lH5);

        for (int i = 0; i < 5; i++) {
            labels.get(i).setText(houses.get(i).name);
            quotes.get(i).setText(houses.get(i).getStatus());
        }

        buildSelectionCards();
        racePane.setVisible(false);
        setupPane.setVisible(true);
    }

    private void buildSelectionCards() {
        cardContainer.getChildren().clear();
        for (House h : houses) {
            VBox card = new VBox(10);
            card.setAlignment(Pos.CENTER);
            card.setStyle("-fx-background-color: #1a241c; -fx-border-color: #d4af37; -fx-border-width: 2; -fx-padding: 15; -fx-cursor: hand;");
            card.setPrefWidth(220);
            card.setPrefHeight(250);

            StackPane imageBox = new StackPane();
            imageBox.setPrefSize(80, 80);
            try {
                Image img = new Image(getClass().getResourceAsStream(h.imagePath));
                if (img.isError()) throw new Exception();
                ImageView imgView = new ImageView(img);
                imgView.setFitWidth(80); imgView.setFitHeight(80);
                imageBox.getChildren().add(imgView);
            } catch (Exception e) {
                imageBox.setStyle("-fx-background-color: #0b120d; -fx-border-color: #d4af37; -fx-border-width: 2;");
                Label initial = new Label(h.name.substring(0, 1));
                initial.setStyle("-fx-text-fill: #d4af37; -fx-font-size: 36px; -fx-font-family: 'Georgia';");
                imageBox.getChildren().add(initial);
            }

            Label name = new Label(h.name);
            name.setStyle("-fx-text-fill: #d4af37; -fx-font-weight: bold; -fx-font-size: 16px; -fx-font-family: 'Georgia';");

            Label stats = new Label(h.getStatsString());
            stats.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px;");

            Label mantra = new Label("\"" + h.mantra + "\"");
            mantra.setStyle("-fx-text-fill: #9ea89f; -fx-font-style: italic; -fx-font-size: 12px;");
            mantra.setWrapText(true);
            mantra.setAlignment(Pos.CENTER);

            card.getChildren().addAll(imageBox, name, stats, mantra);

            card.setOnMouseClicked(e -> {
                selectedHouse = h;
                cardContainer.getChildren().forEach(node -> node.setStyle("-fx-background-color: #1a241c; -fx-border-color: #d4af37; -fx-border-width: 2; -fx-padding: 15; -fx-cursor: hand;"));
                card.setStyle("-fx-background-color: #2b3d2f; -fx-border-color: #ffdb58; -fx-border-width: 4; -fx-padding: 13; -fx-cursor: hand;");
            });
            cardContainer.getChildren().add(card);
        }
    }

    @FXML
    private void handlePlaceBet() {
        String pName = txtPlayerName.getText().trim();
        if (pName.isEmpty() || selectedHouse == null) {
            txtBets.appendText(">> ERROR: Select a house and enter a name, mate.\n");
            return;
        }

        selectedHouse.addBettor(pName);
        totalBets++;
        txtBets.appendText(">> Wager logged: " + pName + " backs " + selectedHouse.name + "\n");
        txtPlayerName.clear();

        if (totalBets >= 2) btnStartRaceMenu.setDisable(false);
    }

    @FXML
    private void handleSpeedToggle() {
        // Cycles between 1x, 2x, and 4x speed
        speedMultiplier *= 2;
        if (speedMultiplier > 4) speedMultiplier = 1;
        btnSpeed.setText("SPEED: " + speedMultiplier + "x");
    }

    @FXML
    private void handleStartRace() {
        setupPane.setVisible(false);
        racePane.setVisible(true);
        lblStatus.setText("THE RACE IS ON...");
        btnExit.setVisible(false);
        btnNextRace.setVisible(false);
        btnSpeed.setVisible(true);

        isRaceOver = false;

        for (int i = 0; i < houses.size(); i++) {
            House h = houses.get(i);
            int index = i;

            Thread houseThread = new Thread(() -> {
                while (!isRaceOver && h.getPosition() < FINISH_LINE) {
                    h.takeTurn();
                    Platform.runLater(() -> updateHouseUI(index, h));

                    try {
                        // Slower base speed (250ms) allows for reading dialogue!
                        // Divider speeds it up when the toggle is clicked.
                        Thread.sleep(250 / speedMultiplier);
                    }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }

                    if (h.getPosition() >= FINISH_LINE && !isRaceOver) {
                        isRaceOver = true;
                        Platform.runLater(() -> declareWinner(h));
                    }
                }
            });
            houseThread.setDaemon(true);
            houseThread.start();
        }
    }

    private void updateHouseUI(int index, House h) {
        // Move both the house and the speech bubble!
        nodes.get(index).setTranslateX(h.getPosition());
        quotes.get(index).setTranslateX(h.getPosition());

        staminas.get(index).setProgress(h.getStaminaPercent());
        quotes.get(index).setText(h.getStatus());

        if (h.isExhausted()) {
            staminas.get(index).setStyle("-fx-accent: #8b0000;"); // Deep Crimson
            quotes.get(index).setStyle("-fx-text-fill: #ff6666; -fx-font-style: italic; -fx-background-color: #0b120dBB; -fx-padding: 3 8; -fx-background-radius: 8; -fx-border-color: #8b0000; -fx-border-radius: 8; -fx-border-width: 1;");
        } else {
            staminas.get(index).setStyle("-fx-accent: #d4af37;"); // Gold
            quotes.get(index).setStyle("-fx-text-fill: #ffffff; -fx-font-style: italic; -fx-background-color: #0b120dBB; -fx-padding: 3 8; -fx-background-radius: 8; -fx-border-color: #d4af37; -fx-border-radius: 8; -fx-border-width: 1;");
        }
    }

    private void declareWinner(House w) {
        String winnerText = "WINNER: " + w.name + "!\n";
        if (w.getBettors().isEmpty()) {
            winnerText += "The House takes it all! No winning bets.";
        } else {
            winnerText += "Payouts to: " + String.join(", ", w.getBettors());
        }

        lblStatus.setText(winnerText);
        lblStatus.setStyle("-fx-text-fill: #d4af37; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Georgia';");
        btnExit.setVisible(true);
        btnNextRace.setVisible(true);
        btnSpeed.setVisible(false); // Hide speed toggle when race ends
    }

    @FXML
    private void handleNextRace() {
        totalBets = 0;
        selectedHouse = null;
        txtBets.clear();
        btnStartRaceMenu.setDisable(true);

        for (House h : houses) { h.resetForNextRace(); }
        for (int i = 0; i < 5; i++) { updateHouseUI(i, houses.get(i)); }

        cardContainer.getChildren().forEach(node -> node.setStyle("-fx-background-color: #1a241c; -fx-border-color: #d4af37; -fx-border-width: 2; -fx-padding: 15; -fx-cursor: hand;"));

        racePane.setVisible(false);
        setupPane.setVisible(true);
    }

    @FXML
    private void onExitButtonClicked() {
        isRaceOver = true;
        quitToLobby();
    }
}