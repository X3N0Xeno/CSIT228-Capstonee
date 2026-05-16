package com.capstone.javabox.csit228.games.fullhouse;

import com.capstone.javabox.csit228.games.JavaboxAbstractController;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FullHouseController extends JavaboxAbstractController {

    @FXML private VBox setupPane;
    @FXML private FlowPane cardContainer;
    @FXML private TextField txtPlayerName;
    @FXML private VBox setupLedgerContainer;
    @FXML private Button btnStartRaceMenu;
    @FXML private Button btnExit;
    @FXML private ComboBox<Integer> comboRacerCount;

    @FXML private VBox racePane;
    @FXML private VBox trackContainer;
    @FXML private Label lblStatus;
    @FXML private VBox activeLedgerContainer;
    @FXML private Button btnNextRace, btnSpeed;

    private List<House> masterRoster;
    private List<House> activeHouses = new ArrayList<>();
    private List<Rectangle> nodes = new ArrayList<>();
    private List<ProgressBar> staminas = new ArrayList<>();
    private List<Label> quotes = new ArrayList<>();

    private final double FINISH_LINE = 1000.0;
    private int totalBets = 0;
    private House selectedHouse = null;
    private volatile boolean isRaceOver = false;
    private int speedMultiplier = 1;

    @FXML
    public void initialize() {
        masterRoster = Arrays.asList(
                new House("The Shelby Estate", "By order of the hopping blinders.", "shelby.png", 8, "£4.5M", 3, "#8b0000", "BY ORDER OF THE BLINDERS!"),
                new House("Shamrock Manor", "Gold in the basement, speed in the bricks.", "shamrock.png", 6, "£850k", 5, "#228b22", "POT OF GOLD UNLEASHED!"),
                new House("The Rusty Garrison", "Smells like stout and victory.", "garrison.png", 7, "£120k", 4, "#cd853f", "RUSTY JUGGERNAUT!"),
                new House("Gilded Cottage", "Lavish, small, suspiciously fast.", "cottage.png", 5, "£2.1M", 6, "#ffd700", "GILDED GLORY!"),
                new House("Cobblestone Keep", "Slow, steady, heavily rigged.", "keep.png", 6, "£600k", 5, "#a9a9a9", "STONE WALL STANDING!"),
                new House("The Emerald Pub", "Spilled ale, slippery tracks.", "pub.png", 8, "£250k", 3, "#32cd32", "LIQUID COURAGE!"),
                new House("Ironforge Mill", "Grinding gears, broken bets.", "mill.png", 5, "£400k", 6, "#ff4500", "GRINDING GEARS OF FATE!"),
                new House("Velvet Parlor", "Elegance comes at crawling pace.", "parlor.png", 7, "£3.2M", 4, "#800080", "VELVET THUNDER!"),
                new House("Whisperers' Alley", "Secrets move faster than wind.", "alley.png", 6, "£150k", 5, "#4b0082", "WHISPERS IN THE DARK!"),
                new House("Banshee's Wail", "Terrifying unpredictability.", "wail.png", 8, "£880k", 3, "#00ffff", "SHATTERING WAIL!"),
                new House("The Brass Vault", "Heavy pockets, heavy steps.", "vault.png", 5, "£5.0M", 6, "#b8860b", "BANK IN THE BANK!"),
                new House("Leprechaun's Loot", "Pure unadulterated Irish luck.", "loot.png", 6, "Priceless", 5, "#adff2f", "LEPRECHAUN'S WRATH!"),
                new House("The Crooked Spire", "Leaning dangerously close to victory.", "spire.png", 7, "£330k", 4, "#8a2be2", "CROOKED ASCENSION!")
        );

        comboRacerCount.getItems().addAll(6, 7, 8, 9, 10, 11, 12, 13);
        comboRacerCount.setValue(8);
        comboRacerCount.setOnAction(e -> updateRacerCount());

        updateRacerCount();

        racePane.setVisible(false);
        setupPane.setVisible(true);

        Platform.runLater(() -> {
            Stage stage = (Stage) setupPane.getScene().getWindow();
            if (stage != null) {
                stage.setMinWidth(1280);
                stage.setMinHeight(720);
                stage.setResizable(true);
                stage.setMaximized(true);
            }
        });
    }

    private void updateRacerCount() {
        int count = comboRacerCount.getValue();
        activeHouses = new ArrayList<>(masterRoster.subList(0, count));

        totalBets = 0;
        selectedHouse = null;
        txtPlayerName.clear();
        setupLedgerContainer.getChildren().clear();
        btnStartRaceMenu.setDisable(true);

        for (House h : masterRoster) h.resetForNextRace();

        buildSelectionCards();
        buildRaceTracks();
    }

    private void buildSelectionCards() {
        cardContainer.getChildren().clear();
        for (House h : activeHouses) {
            VBox card = new VBox(5);
            card.setAlignment(Pos.CENTER);
            card.setStyle("-fx-background-color: #1a241c; -fx-border-color: " + h.colorHex + "; -fx-border-width: 2; -fx-padding: 8; -fx-cursor: hand;");
            card.setPrefWidth(160);
            card.setPrefHeight(190);

            StackPane imageBox = new StackPane();
            imageBox.setPrefSize(40, 40);
            try {
                ImageView imgView = new ImageView(new Image(getClass().getResourceAsStream(h.imagePath)));
                imgView.setFitWidth(40); imgView.setFitHeight(40);
                imageBox.getChildren().add(imgView);
            } catch (Exception e) {
                imageBox.setStyle("-fx-background-color: #0b120d; -fx-border-color: " + h.colorHex + "; -fx-border-width: 1;");
                Label initial = new Label(h.name.substring(0, 1));
                initial.setStyle("-fx-text-fill: " + h.colorHex + "; -fx-font-size: 18px; -fx-font-family: 'Georgia';");
                imageBox.getChildren().add(initial);
            }

            Label name = new Label(h.name);
            name.setStyle("-fx-text-fill: " + h.colorHex + "; -fx-font-weight: bold; -fx-font-size: 12px; -fx-font-family: 'Georgia';");

            Label stats = new Label(h.getStatsString());
            stats.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 11px;");
            stats.setTextAlignment(TextAlignment.CENTER);

            card.getChildren().addAll(imageBox, name, stats);

            card.setOnMouseClicked(e -> {
                selectedHouse = h;
                for (int i = 0; i < cardContainer.getChildren().size(); i++) {
                    cardContainer.getChildren().get(i).setStyle("-fx-background-color: #1a241c; -fx-border-color: " + activeHouses.get(i).colorHex + "; -fx-border-width: 2; -fx-padding: 8; -fx-cursor: hand;");
                }
                card.setStyle("-fx-background-color: #2b3d2f; -fx-border-color: #ffffff; -fx-border-width: 4; -fx-padding: 6; -fx-cursor: hand;");
            });
            cardContainer.getChildren().add(card);
        }
    }

    private void buildRaceTracks() {
        trackContainer.getChildren().clear();
        nodes.clear(); staminas.clear(); quotes.clear();

        for (House h : activeHouses) {
            Pane track = new Pane();
            VBox.setVgrow(track, Priority.ALWAYS);
            track.setStyle("-fx-border-color: #2b3d2f; -fx-border-width: 0 0 1 0;");

            Line finishLineSegment = new Line();
            finishLineSegment.setStartY(0);
            finishLineSegment.endYProperty().bind(track.heightProperty());
            finishLineSegment.startXProperty().bind(track.widthProperty().subtract(60));
            finishLineSegment.endXProperty().bind(track.widthProperty().subtract(60));
            finishLineSegment.setStroke(Color.web("#d4af37"));
            finishLineSegment.setStrokeWidth(4);
            finishLineSegment.getStrokeDashArray().addAll(8d, 8d);
            finishLineSegment.setManaged(false);

            Label nameLbl = new Label(h.name);
            nameLbl.setStyle("-fx-text-fill: " + h.colorHex + "; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 12px;");
            nameLbl.setLayoutX(5);
            nameLbl.setLayoutY(5);

            ProgressBar stamina = new ProgressBar(1.0);
            stamina.setPrefWidth(90);
            stamina.setPrefHeight(10);
            stamina.setStyle("-fx-accent: " + h.colorHex + ";");
            stamina.setLayoutX(5);

            Rectangle node = new Rectangle(25, 25);
            node.setFill(Color.web(h.colorHex));
            node.setStroke(Color.web("#ffffff"));
            node.setLayoutX(100);

            Label quoteLbl = new Label(h.getStatus());
            quoteLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-style: italic; -fx-font-size: 10px; -fx-background-color: #0b120dBB; -fx-padding: 2 5; -fx-background-radius: 5; -fx-border-color: " + h.colorHex + "; -fx-border-radius: 5;");
            quoteLbl.setLayoutX(100);

            track.heightProperty().addListener((obs, oldVal, newVal) -> {
                double hTrack = newVal.doubleValue();
                // Vertically stack: name (top), stamina (immediately below)
                stamina.setLayoutY(nameLbl.getLayoutY() + nameLbl.getHeight() + 2);
                node.setLayoutY((hTrack / 2) - 12.5); // Center racer vertically
                quoteLbl.setLayoutY(node.getLayoutY() - 20); // Position quote relative to racer
            });

            track.getChildren().addAll(finishLineSegment, nameLbl, stamina, node, quoteLbl);
            trackContainer.getChildren().add(track);

            nodes.add(node);
            staminas.add(stamina);
            quotes.add(quoteLbl);
        }
    }

    @FXML
    private void handlePlaceBet() {
        String pName = txtPlayerName.getText().trim();
        if (pName.isEmpty() || selectedHouse == null) return;

        selectedHouse.addBettor(pName);
        totalBets++;
        txtPlayerName.clear();

        updateLedgerBoard(setupLedgerContainer);
        if (totalBets >= 2) btnStartRaceMenu.setDisable(false);
    }

    private void updateLedgerBoard(VBox container) {
        container.getChildren().clear();
        for (House h : activeHouses) {
            if (!h.getBettors().isEmpty()) {
                Label houseHeader = new Label(h.name.toUpperCase());
                houseHeader.setStyle("-fx-text-fill: " + h.colorHex + "; -fx-font-weight: bold; -fx-font-size: 14px; -fx-font-family: 'Georgia'; -fx-underline: true; -fx-padding: 5 0 0 0;");
                container.getChildren().add(houseHeader);

                for (String bettor : h.getBettors()) {
                    Label bettorName = new Label(" • " + bettor);
                    bettorName.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 12px; -fx-font-family: 'Courier New';");
                    container.getChildren().add(bettorName);
                }
            }
        }
    }

    @FXML
    private void handleSpeedToggle() {
        speedMultiplier *= 2;
        if (speedMultiplier > 4) speedMultiplier = 1;
        btnSpeed.setText("SPEED: " + speedMultiplier + "x");
    }

    @FXML
    private void handleStartRace() {
        setupPane.setVisible(false);
        racePane.setVisible(true);
        lblStatus.setText("THE RACE IS ON...");
        lblStatus.setStyle("-fx-text-fill: #728075; -fx-font-size: 18px; -fx-font-family: 'Georgia'; -fx-font-style: italic;");

        updateLedgerBoard(activeLedgerContainer);

        btnNextRace.setVisible(false);
        btnSpeed.setVisible(true);

        isRaceOver = false;

        for (int i = 0; i < activeHouses.size(); i++) {
            House h = activeHouses.get(i);
            int index = i;

            Thread houseThread = new Thread(() -> {
                while (!isRaceOver && h.getPosition() < FINISH_LINE) {
                    h.takeTurn();
                    Platform.runLater(() -> updateHouseUI(index, h));

                    try { Thread.sleep(200 / speedMultiplier); }
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
        Rectangle actualNode = nodes.get(index);
        Pane trackPane = (Pane) actualNode.getParent();

        double trackWidth = trackPane.getWidth();
        if (trackWidth < 200) trackWidth = 1000;

        double maxTravelDist = trackWidth - 60 - 100 - actualNode.getWidth();

        double progress = Math.min(h.getPosition() / FINISH_LINE, 1.0);
        double lastProgress = Math.min(h.getLastPosition() / FINISH_LINE, 1.0);

        double currentPixelX = progress * maxTravelDist;
        double lastPixelX = lastProgress * maxTravelDist;

        Rectangle ghost = new Rectangle(actualNode.getWidth(), actualNode.getHeight(), actualNode.getFill());
        ghost.setLayoutX(100);
        ghost.setLayoutY(actualNode.getLayoutY());
        ghost.setTranslateX(lastPixelX);
        ghost.setOpacity(0.5);
        trackPane.getChildren().add(0, ghost);

        FadeTransition ft = new FadeTransition(Duration.millis(300 / speedMultiplier), ghost);
        ft.setToValue(0.0);
        ft.setOnFinished(evt -> trackPane.getChildren().remove(ghost));
        ft.play();

        TranslateTransition tt = new TranslateTransition(Duration.millis(150 / speedMultiplier), actualNode);
        tt.setToX(currentPixelX);
        tt.play();

        TranslateTransition quoteTt = new TranslateTransition(Duration.millis(150 / speedMultiplier), quotes.get(index));
        quoteTt.setToX(currentPixelX);
        quoteTt.play();

        staminas.get(index).setProgress(h.getEnergyPercent());
        quotes.get(index).setText(h.getStatus());

        if (h.isSpecialActive()) {
            staminas.get(index).setStyle("-fx-accent: #ff00ff;");
            quotes.get(index).setStyle("-fx-text-fill: #ff00ff; -fx-font-style: italic; -fx-font-weight: bold; -fx-background-color: #0b120dBB; -fx-padding: 2 5; -fx-background-radius: 5; -fx-border-color: #ff00ff; -fx-border-radius: 5;");
        }
        else if (h.isExhausted()) {
            staminas.get(index).setStyle("-fx-accent: #8b0000;");
            quotes.get(index).setStyle("-fx-text-fill: #ff6666; -fx-font-style: italic; -fx-background-color: #0b120dBB; -fx-padding: 2 5; -fx-background-radius: 5; -fx-border-color: #8b0000; -fx-border-radius: 5;");
        }
        else {
            staminas.get(index).setStyle("-fx-accent: " + h.colorHex + ";");
            quotes.get(index).setStyle("-fx-text-fill: #ffffff; -fx-font-style: italic; -fx-background-color: #0b120dBB; -fx-padding: 2 5; -fx-background-radius: 5; -fx-border-color: " + h.colorHex + "; -fx-border-radius: 5;");
        }
    }

    private void declareWinner(House w) {
        String winnerText;
        if (w.getBettors().isEmpty()) {
            winnerText = "PAYOUT GOES TO: THE HOUSE TAKES IT ALL! (" + w.name + " Wins)";
        } else {
            winnerText = "PAYOUT GOES TO: " + String.join(", ", w.getBettors()) + " (" + w.name + " Wins!)";
        }

        lblStatus.setText(winnerText.toUpperCase());
        lblStatus.setStyle("-fx-text-fill: " + w.colorHex + "; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Georgia';");
        btnNextRace.setVisible(true);
        btnSpeed.setVisible(false);

        activeLedgerContainer.getChildren().clear();
        Label leaderHeader = new Label("FINAL PLACEMENTS");
        leaderHeader.setStyle("-fx-text-fill: #d4af37; -fx-font-weight: bold; -fx-font-size: 18px; -fx-font-family: 'Georgia'; -fx-underline: true; -fx-padding: 0 0 10 0;");
        activeLedgerContainer.getChildren().add(leaderHeader);

        List<House> ranked = new ArrayList<>(activeHouses);
        ranked.sort((h1, h2) -> Double.compare(h2.getPosition(), h1.getPosition()));

        for (int i = 0; i < ranked.size(); i++) {
            House h = ranked.get(i);
            String suffix = (i == 0) ? " [WINNER]" : "";
            Label rankLbl = new Label((i + 1) + ". " + h.name.toUpperCase() + suffix);
            rankLbl.setStyle("-fx-text-fill: " + h.colorHex + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'Courier New';");
            activeLedgerContainer.getChildren().add(rankLbl);
        }
    }

    @FXML
    private void handleNextRace() {
        totalBets = 0;
        selectedHouse = null;
        setupLedgerContainer.getChildren().clear();
        activeLedgerContainer.getChildren().clear();
        btnStartRaceMenu.setDisable(true);

        for (House h : activeHouses) { h.resetForNextRace(); }

        for (int i = 0; i < activeHouses.size(); i++) {
            nodes.get(i).setTranslateX(0);
            quotes.get(i).setTranslateX(0);
            staminas.get(i).setProgress(1.0);
            staminas.get(i).setStyle("-fx-accent: " + activeHouses.get(i).colorHex + ";");
            quotes.get(i).setText(activeHouses.get(i).getStatus());
            quotes.get(i).setStyle("-fx-text-fill: #ffffff; -fx-font-style: italic; -fx-background-color: #0b120dBB; -fx-padding: 2 5; -fx-background-radius: 5; -fx-border-color: " + activeHouses.get(i).colorHex + "; -fx-border-radius: 5;");
        }

        cardContainer.getChildren().forEach(node -> node.setStyle("-fx-background-color: #1a241c; -fx-border-color: " + activeHouses.get(cardContainer.getChildren().indexOf(node)).colorHex + "; -fx-border-width: 2; -fx-padding: 8; -fx-cursor: hand;"));

        racePane.setVisible(false);
        setupPane.setVisible(true);
    }

    @FXML
    private void onExitButtonClicked() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Leave the Parlor");
        alert.setHeaderText("Return to Lobby?");
        alert.setContentText("Placed wagers will be abandoned.");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1a241c; -fx-font-family: 'Georgia';");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                isRaceOver = true;
                quitToLobby();
            }
        });
    }
}