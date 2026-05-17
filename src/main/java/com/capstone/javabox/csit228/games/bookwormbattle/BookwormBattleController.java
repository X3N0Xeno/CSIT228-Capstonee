package com.capstone.javabox.csit228.games.bookwormbattle;

import com.capstone.javabox.csit228.database.BookwormBattleDAO;
import com.capstone.javabox.csit228.games.JavaboxAbstractController;
import com.capstone.javabox.csit228.games.bookwormbattle.GameConstants.*;
import com.capstone.javabox.csit228.utils.SoundManager;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;

public class BookwormBattleController extends JavaboxAbstractController {

    @FXML private GridPane p1GridPane, p2GridPane;
    @FXML private Label p1HealthLabel, p2HealthLabel, p1ScrambleLabel, p2ScrambleLabel;
    @FXML private Label turnIndicator, wordPreview, timerLabel;
    @FXML private VBox p1Inventory, p2Inventory;
    @FXML private VBox p1StatusBox, p2StatusBox;
    @FXML private HBox gameUI;

    private Player p1, p2, currentPlayer;
    private Timeline turnTimer;
    private int secondsRemaining = 30;
    private Set<String> dictionary = new HashSet<>();
    private Random random = new Random();
    private final int GRID_SIZE = 4;
    private final String DATA = "EEEEEEEEEEEEAAAAAAAAAIIIIIIIIIOOOOOOOONNNNNNRRRRRRTTTTTTLLLLSSSSUUUUUDDDDGGGBBCCMMPPFFHHVVWWYYKJXQZ";
    private CardType pendingCardAction = null;
    private int totalPhasesPassed = 0;

    @FXML private VBox wheelOverlay;
    @FXML private Label wheelResultLabel, wheelStatusLabel;

    @FXML
    public void initialize() {
        loadDictionary();
        setupTimer();
    }

    // --- RECEIVE PLAYER ALIASES ---
    public void startCountdown(String p1Alias, String p2Alias) {
        p1 = new Player(p1Alias, p1GridPane);
        p2 = new Player(p2Alias, p2GridPane);
        currentPlayer = p1;

        totalPhasesPassed = 0;

        setupBoard(p1);
        setupBoard(p2);
        runCountdownSequence();
    }

    private void runCountdownSequence() {
        SoundManager.playMusic("music_bookworm_minigame.mp3");
        p1GridPane.setDisable(true); p2GridPane.setDisable(true);
        final int[] count = {3};
        wordPreview.setStyle("-fx-font-size: 60; -fx-text-fill: #e74c3c;");

        Timeline countdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (count[0] > 0) {
                wordPreview.setText(String.valueOf(count[0]));
                SoundManager.playSFX("sfx_" + count[0] + ".wav");
                count[0]--;
            } else {
                wordPreview.setText("GO!");
                wordPreview.setStyle("-fx-font-size: 42; -fx-text-fill: #ffd32a;");
                SoundManager.playSFX("sfx_go.wav");
                p1GridPane.setDisable(false); p2GridPane.setDisable(false);
                updateUI();
                startTurnTimer();
            }
        }));
        countdown.setCycleCount(4);
        countdown.play();
    }

    // --- GAME LOGIC ---

    @FXML
    private void onSpell() {
        String word = currentPlayer.getSelectedWordString().toUpperCase();

        if (word.length() < 3) {
            SoundManager.playSFX("sfx_error.wav");
            wordPreview.setText("TOO SHORT!"); return; }
        if (!dictionary.contains(word)) {
            SoundManager.playSFX("sfx_error.wav");
            wordPreview.setText("INVALID WORD!"); return; }

        SoundManager.playSFX("sfx_spell.wav");
        turnTimer.stop();
        Player enemy = (currentPlayer == p1) ? p2 : p1;

        for (Tile t : currentPlayer.selectedTiles) {
            if (t.gemType != null) {
                applyGemEffect(t.gemType, enemy);
                t.gemType = null;
            }
        }

        double damage = word.length() * 0.5;
        if (currentPlayer.hasStrength) { damage *= 2; currentPlayer.hasStrength = false; }
        if (currentPlayer.isPoweredDown) { damage /= 2; currentPlayer.isPoweredDown = false; }
        enemy.takeDamage(damage);

        processGravity(currentPlayer);
        spawnGemAtRandom(currentPlayer, word.length());

        currentPlayer.refreshBoard();
        updateUI();

        triggerRandomRewardWheel();
    }

    private void endTurn() {
        turnTimer.stop();
        clearSelection();

        totalPhasesPassed++;

        if (totalPhasesPassed % 2 == 0) {
            System.out.println("Cycle Complete (2 Phases)! Dropping Potions...");
            giveRandomPotions();
            wordPreview.setText("POTION DROP!");
            wordPreview.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 24;");
        }

        currentPlayer = (currentPlayer == p1) ? p2 : p1;

        if (currentPlayer.isFrozen) {
            currentPlayer.isFrozen = false;
            endTurn();
            return;
        }

        if (currentPlayer.poisonTicks > 0) {
            currentPlayer.takeDamage(1.0);
            currentPlayer.poisonTicks--;
        }

        updateUI();

        if (p1.hearts <= 0 || p2.hearts <= 0) {
            checkGameOver();
        } else {
            startTurnTimer();
        }
    }

    // --- INVENTORY ---

    private void renderInv(Player p, VBox box) {
        box.getChildren().clear();
        box.setSpacing(5);

        for (int i = 0; i < p.cards.size(); i++) {
            GameConstants.CardType card = p.cards.get(i);
            Button b = new Button(card.label);
            b.setDisable(p != currentPlayer);
            b.setStyle("-fx-font-size: 11px; -fx-background-color: #34495e; -fx-text-fill: white; -fx-pref-width: 150; -fx-cursor: hand;");
            b.setOnAction(e -> {
                applyCard(card, p);
                p.cards.remove(card);
                updateUI();
            });
            box.getChildren().add(b);
        }

        p.potions.forEach((type, qty) -> {
            if (qty > 0) {
                Button b = new Button(type.name() + " (" + qty + ")");
                b.setDisable(p != currentPlayer);
                b.setStyle("-fx-font-size: 11px; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-pref-width: 150; -fx-cursor: hand;");
                b.setOnAction(e -> {
                    usePotion(type, p);
                    p.potions.put(type, qty - 1);
                    updateUI();
                });
                box.getChildren().add(b);
            }
        });
    }

    private void usePotion(PotionType t, Player p) {
        if (t == PotionType.HEALING) {
            p.heal(3);
            SoundManager.playSFX("sfx_heal.wav");
        } else if (t == PotionType.STRENGTH) {
            p.hasStrength = true;
            SoundManager.playSFX("sfx_powerup2.wav");
        }
        else if (t == PotionType.PURIFY) {
            p.poisonTicks = 0; p.isPoweredDown = false;
            p.isFrozen = false;
            SoundManager.playSFX("sfx_purify.wav");
        }
    }

    private void applyCard(CardType c, Player p) {
        Player enemy = (p == p1) ? p2 : p1;

        switch (c) {
            case SCRAMBLE_COL:
            case SCRAMBLE_ROW:
            case LOCKDOWN:
            case GEM_STEAL:
                pendingCardAction = c;
                updateUI();
                return;

            case HEALING:
                p.heal(5.0);
                break;

            case HELP_ME:
                p.scrambles++;
                break;

            case SHIELD:
                p.hasShield = true;
                break;

            case SHOP_DELUXE:
                p.addPotion(PotionType.HEALING);
                p.addPotion(PotionType.STRENGTH);
                p.addPotion(PotionType.PURIFY);
                break;

            case GEM_NUKE:
                for (int r = 0; r < 4; r++) {
                    for (int col = 0; col < 4; col++) {
                        enemy.board[r][col].setGem(null);
                    }
                }
                enemy.refreshBoard();
                break;
        }

        p.cards.remove(c);
        updateUI();
    }

    // --- UTILS ---

    private void updateUI() {
        p1HealthLabel.setText(String.format("Hearts: %.1f/20.0", p1.hearts));
        p2HealthLabel.setText(String.format("Hearts: %.1f/20.0", p2.hearts));
        p1ScrambleLabel.setText("Scrambles: " + p1.scrambles);
        p2ScrambleLabel.setText("Scrambles: " + p2.scrambles);

        turnIndicator.setText(currentPlayer.name + "'s Turn");

        if (pendingCardAction != null) {
            Player enemy = (currentPlayer == p1) ? p2 : p1;
            enemy.gridPane.setOpacity(1.0);
            enemy.gridPane.setMouseTransparent(false);
            enemy.gridPane.setStyle("-fx-border-color: #3498db; -fx-border-width: 5; -fx-border-radius: 15;");

            currentPlayer.gridPane.setOpacity(0.3);
            currentPlayer.gridPane.setMouseTransparent(true);

            wordPreview.setText("TARGETING: " + pendingCardAction.label);
            wordPreview.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; -fx-font-size: 28;");
        } else {
            p1GridPane.setOpacity(currentPlayer == p1 ? 1.0 : 0.4);
            p1GridPane.setMouseTransparent(currentPlayer != p1);
            p1GridPane.setStyle("-fx-border-color: #ff4757; -fx-border-width: 2; -fx-border-radius: 15;");

            p2GridPane.setOpacity(currentPlayer == p2 ? 1.0 : 0.4);
            p2GridPane.setMouseTransparent(currentPlayer != p2);
            p2GridPane.setStyle("-fx-border-color: #1e90ff; -fx-border-width: 2; -fx-border-radius: 15;");

            wordPreview.setStyle("-fx-text-fill: #ffd32a; -fx-font-weight: bold; -fx-font-size: 42;");
        }

        renderInv(p1, p1Inventory);
        renderInv(p2, p2Inventory);
        renderStatus(p1, p1StatusBox);
        renderStatus(p2, p2StatusBox);
    }

    private void renderStatus(Player p, VBox box) {
        box.getChildren().clear();
        box.setSpacing(5);

        if (p.isFrozen) box.getChildren().add(createStatusIndicator("❄ FROZEN", "#2980b9"));
        if (p.poisonTicks > 0) box.getChildren().add(createStatusIndicator("☣ POISON (" + p.poisonTicks + "T)", "#8e44ad"));
        if (p.isPoweredDown) box.getChildren().add(createStatusIndicator("↓ POWER DOWN", "#d35400"));
        if (p.hasStrength) box.getChildren().add(createStatusIndicator("↑ STRENGTH UP", "#27ae60"));
        if (p.hasShield) box.getChildren().add(createStatusIndicator("🛡 SHIELDED", "#16a085"));
    }

    private Button createStatusIndicator(String text, String color) {
        Button b = new Button(text);
        b.setDisable(true);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-opacity: 1.0; " +
                "-fx-font-weight: bold; -fx-pref-width: 160; -fx-font-size: 11px; -fx-border-color: white; -fx-border-width: 0.5;");
        return b;
    }

    private void checkGameOver() {
        if (p1.hearts <= 0) p1.hearts = 0;
        if (p2.hearts <= 0) p2.hearts = 0;

        if (p1.hearts <= 0 || p2.hearts <= 0) {
            if (turnTimer != null) turnTimer.stop();
            SoundManager.stopMusic();
            SoundManager.playSFX("sfx_victory.wav");

            // --- DATABASE UPLOAD ---
            String winnerAlias = p1.hearts > 0 ? p1.name : p2.name;
            BookwormBattleDAO.saveMatch(p1.name, p2.name, winnerAlias);

            javafx.application.Platform.runLater(() -> {
                String winner = p1.hearts > 0 ? p1.name : p2.name;

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("GAME OVER");
                alert.setHeaderText(winner + " is the Victor!");
                alert.setContentText("Would you like to play again or return to the JavaBox lobby?");

                ButtonType btnPlayAgain = new ButtonType("Play Again");
                ButtonType btnLobby = new ButtonType("Return to Lobby");
                alert.getButtonTypes().setAll(btnPlayAgain, btnLobby);

                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == btnPlayAgain) {
                    startCountdown(p1.name, p2.name);
                } else {
                    quitToLobby();
                }
            });
        }
    }

    private void setupBoard(Player p) {
        p.gridPane.getChildren().clear();
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (p.board[r][c] == null || p.board[r][c].gemType == null) {
                    Tile tile = new Tile(r, c, generateLetter());
                    p.board[r][c] = tile;
                } else {
                    p.board[r][c].setSelected(false);
                }
                p.gridPane.add(p.board[r][c].pane, c, r);
                Tile currentTile = p.board[r][c];
                currentTile.pane.setOnMouseClicked(e -> handleMouseClick(e, p, currentTile));
            }
        }
    }

    private void setupTimer() {
        turnTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;
            if (timerLabel != null) timerLabel.setText("Time: " + secondsRemaining + "s");
            if (secondsRemaining <= 0) onSkip();
        }));
        turnTimer.setCycleCount(Timeline.INDEFINITE);
    }

    private void startTurnTimer() {
        secondsRemaining = 30;
        if (timerLabel != null) timerLabel.setText("Time: 30s");
        turnTimer.playFromStart();
    }

    private void handleMouseClick(javafx.scene.input.MouseEvent e, Player p, Tile tile) {
        if (pendingCardAction != null) {
            Player enemy = (currentPlayer == p1) ? p2 : p1;
            if (p == enemy) {
                executeTargetedCard(pendingCardAction, enemy, tile);
                currentPlayer.cards.remove(pendingCardAction);
                pendingCardAction = null;
                updateUI();
            }
            return;
        }

        if (e.getButton() == MouseButton.SECONDARY) {
            onClear();
        } else {
            handleTileClick(p, tile);
        }
    }

    private void executeTargetedCard(CardType type, Player enemy, Tile targetTile) {
        Random r = new Random();
        switch (type) {
            case LOCKDOWN -> {
                targetTile.isLocked = true;
                SoundManager.playSFX("sfx_lockdown.wav");
            }
            case SCRAMBLE_ROW -> {
                for (int i = 0; i < 4; i++) {
                    if (enemy.board[targetTile.r][i].gemType == null)
                        enemy.board[targetTile.r][i].letter = "JKXYZQ".charAt(r.nextInt(6));
                }
                SoundManager.playSFX("sfx_rowcolscr.wav");
            }
            case SCRAMBLE_COL -> {
                for (int i = 0; i < 4; i++) {
                    if (enemy.board[i][targetTile.c].gemType == null)
                        enemy.board[i][targetTile.c].letter = "JKXYZQ".charAt(r.nextInt(6));
                }
                SoundManager.playSFX("sfx_rowcolscr.wav");
            }
            case GEM_STEAL -> {
                if (targetTile.gemType != null) {
                    GemType stolen = targetTile.gemType;
                    targetTile.setGem(null);
                    spawnSpecificGem(currentPlayer, stolen);
                }
            }
        }
        enemy.refreshBoard();
    }

    private void handleTileClick(Player p, Tile tile) {
        if (p != currentPlayer || tile.isLocked) return;
        SoundManager.playSFX("sfx_tile_select.wav");
        if (p.selectedTiles.contains(tile)) {
            int index = p.selectedTiles.indexOf(tile);
            List<Tile> toRemove = new ArrayList<>(p.selectedTiles.subList(index, p.selectedTiles.size()));
            for (Tile tr : toRemove) {
                tr.setSelected(false);
                p.selectedTiles.remove(tr);
            }
        } else {
            p.selectedTiles.add(tile);
            tile.setSelected(true);
        }
        wordPreview.setText(p.getSelectedWordString());
    }

    private void spawnGemAtRandom(Player p, int len) {
        GemType type = null;
        if (len == 5) type = GemType.POISON;
        else if (len == 6) type = GemType.HEAL;
        else if (len == 7) type = GemType.POWER_DOWN;
        else if (len >= 8) type = GemType.FREEZE;

        if (type != null) {
            List<Tile> availableTiles = new ArrayList<>();
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    Tile t = p.board[r][c];
                    if (t != null && t.gemType == null) {
                        availableTiles.add(t);
                    }
                }
            }
            if (!availableTiles.isEmpty()) {
                Tile target = availableTiles.get(random.nextInt(availableTiles.size()));
                target.setGem(type);
            }
        }
        SoundManager.playSFX("sfx_gemspawn.wav");
    }

    private void applyGemEffect(GemType type, Player enemy) {
        switch (type) {
            case POISON -> { enemy.poisonTicks = 2; SoundManager.playSFX("sfx_poison.wav"); }
            case HEAL -> { currentPlayer.heal(2.5); SoundManager.playSFX("sfx_heal.wav"); }
            case POWER_DOWN -> { enemy.isPoweredDown = true; SoundManager.playSFX("sfx_powerdown.wav"); }
            case FREEZE -> { enemy.isFrozen = true; SoundManager.playSFX("sfx_frozen.wav"); }
        }
    }

    @FXML
    private void onScramble() {
        if (currentPlayer.scrambles > 0) {
            onClear();
            currentPlayer.scrambles--;
            setupBoard(currentPlayer);
            updateUI();
        } else {
            wordPreview.setText("NO CHARGES!");
        }
    }

    private Tile findRandomGem(Player p) {
        List<Tile> gems = new ArrayList<>();
        for (Tile[] row : p.board) {
            for (Tile t : row) if (t.gemType != null) gems.add(t);
        }
        return gems.isEmpty() ? null : gems.get(new Random().nextInt(gems.size()));
    }

    private void spawnSpecificGem(Player p, GemType type) {
        List<Tile> normal = new ArrayList<>();
        for (Tile[] row : p.board) {
            for (Tile t : row) if (t.gemType == null) normal.add(t);
        }
        if (!normal.isEmpty()) normal.get(0).setGem(type);
    }

    @FXML
    private void onClear() {
        SoundManager.playSFX("sfx_clear.wav");
        clearSelection();
    }

    private void clearSelection() {
        if (p1 != null) {
            for (Tile t : p1.selectedTiles) t.setSelected(false);
            p1.selectedTiles.clear();
        }
        if (p2 != null) {
            for (Tile t : p2.selectedTiles) t.setSelected(false);
            p2.selectedTiles.clear();
        }
        wordPreview.setText("");
    }

    @FXML
    private void onSkip() {
        endTurn();
    }

    private void triggerRandomRewardWheel() {
        wheelOverlay.setVisible(true);
        gameUI.setDisable(true);

        CardType[] allCards = CardType.values();
        CardType winningCard = allCards[random.nextInt(allCards.length)];

        final int totalFlickers = 15;
        final int[] currentFlicker = {0};
        final double[] currentDelay = {0.05};

        runFlickerStep(allCards, winningCard, totalFlickers, currentFlicker, currentDelay);
    }

    private void runFlickerStep(CardType[] allCards, CardType winningCard, int total, int[] current, double[] delay) {
        Timeline flicker = new Timeline(new KeyFrame(Duration.seconds(delay[0]), e -> {
            SoundManager.playSFX("sfx_buttonclick.wav");
            current[0]++;

            if (current[0] < total) {
                wheelStatusLabel.setText(allCards[random.nextInt(allCards.length)].label);
                delay[0] *= 1.15;
                runFlickerStep(allCards, winningCard, total, current, delay);
            } else {
                revealFinalReward(winningCard);
            }
        }));
        flicker.play();
    }

    private void revealFinalReward(CardType winner) {
        SoundManager.playSFX("sfx_reward.wav");
        wheelStatusLabel.setText(winner.label);
        wheelStatusLabel.setTextFill(Color.web("#ffd32a"));
        wheelResultLabel.setText("REWARD GRANTED: " + winner.label);

        Timeline pause = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            currentPlayer.addCard(winner);
            wheelStatusLabel.setTextFill(Color.WHITE);
            wheelOverlay.setVisible(false);
            gameUI.setDisable(false);
            endTurn();
        }));
        pause.play();
    }

    private void giveRandomPotions() {
        PotionType[] pts = PotionType.values();
        p1.addPotion(pts[random.nextInt(pts.length)]);
        p2.addPotion(pts[random.nextInt(pts.length)]);
    }

    @Override
    public void setQuitCallback(Runnable quitCallback) {
        this.quitCallback = quitCallback;
    }

    private void loadDictionary() {
        try (Scanner scanner = new Scanner(getClass().getResourceAsStream("dictionary.txt"))) {
            while (scanner.hasNextLine()) {
                dictionary.add(scanner.nextLine().trim().toUpperCase());
            }
        } catch (Exception e) {
            System.err.println("Could not load dictionary.txt! All words will be invalid.");
            e.printStackTrace();
        }
    }

    private void processGravity(Player p) {
        for (Tile t : p.selectedTiles) {
            t.letter = ' ';
            t.gemType = null;
            t.setSelected(false);
        }
        p.selectedTiles.clear();

        for (int c = 0; c < 4; c++) {
            for (int r = 3; r >= 0; r--) {
                if (p.board[r][c].letter == ' ') {
                    for (int k = r - 1; k >= 0; k--) {
                        if (p.board[k][c].letter != ' ') {
                            p.board[r][c].letter = p.board[k][c].letter;
                            p.board[r][c].gemType = p.board[k][c].gemType;

                            p.board[k][c].letter = ' ';
                            p.board[k][c].gemType = null;
                            break;
                        }
                    }
                }
            }
            for (int r = 0; r < 4; r++) {
                if (p.board[r][c].letter == ' ') {
                    p.board[r][c].letter = generateLetter();
                    p.board[r][c].gemType = null;
                }
            }
        }
        p.refreshBoard();
    }

    @FXML
    private void onQuitToMenu() {
        SoundManager.stopMusic();
        if (turnTimer != null) turnTimer.stop();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bookwormbattle-menu.fxml"));
            Parent menuRoot = loader.load();
            BookwormBattleMenuController menuController = loader.getController();
            menuController.setQuitCallback(this.quitCallback);

            Stage stage = (Stage) gameUI.getScene().getWindow();
            stage.getScene().setRoot(menuRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private char generateLetter() { return DATA.charAt(new Random().nextInt(DATA.length())); }
}