package com.capstone.javabox.csit228.games.bookwormbattle;

import com.capstone.javabox.csit228.games.JavaboxAbstractController;
import com.capstone.javabox.csit228.games.bookwormbattle.GameConstants.*;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
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
    // Top of BookwormBattleController.java
    private int totalPhasesPassed = 0;

    // --- Controller Variables ---
    @FXML private VBox wheelOverlay;
    @FXML private Label wheelResultLabel, wheelStatusLabel;

    @FXML
    public void initialize() {
        loadDictionary();
        setupTimer();
    }

    public void startCountdown() {
        p1 = new Player("Player 1", p1GridPane);
        p2 = new Player("Player 2", p2GridPane);
        currentPlayer = p1;

        totalPhasesPassed = 0;

        setupBoard(p1);
        setupBoard(p2);
        runCountdownSequence();
    }

    private void runCountdownSequence() {
        p1GridPane.setDisable(true); p2GridPane.setDisable(true);
        final int[] count = {3};
        wordPreview.setStyle("-fx-font-size: 60; -fx-text-fill: #e74c3c;");

        Timeline countdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (count[0] > 0) {
                wordPreview.setText(String.valueOf(count[0]));
                count[0]--;
            } else {
                wordPreview.setText("GO!");
                wordPreview.setStyle("-fx-font-size: 42; -fx-text-fill: #ffd32a;");
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

        // 1. Validation
        if (word.length() < 3) { wordPreview.setText("TOO SHORT!"); return; }
        if (!dictionary.contains(word)) { wordPreview.setText("INVALID WORD!"); return; }

        turnTimer.stop();
        Player enemy = (currentPlayer == p1) ? p2 : p1;

        // 2. USE GEMS (Effect happens now)
        for (Tile t : currentPlayer.selectedTiles) {
            if (t.gemType != null) {
                applyGemEffect(t.gemType, enemy);
                t.gemType = null; // Consume it
            }
        }

        // 3. APPLY DAMAGE & CALCULATE
        double damage = word.length() * 0.5;
        if (currentPlayer.hasStrength) { damage *= 2; currentPlayer.hasStrength = false; }
        if (currentPlayer.isPoweredDown) { damage /= 2; currentPlayer.isPoweredDown = false; }
        enemy.takeDamage(damage);

        // 4. MOVE TILES (Gravity)
        // This removes the word and drops new letters down
        processGravity(currentPlayer);

        // 5. SPAWN NEW GEM (Must happen AFTER gravity)
        // We spawn it on the "New" board layout
        spawnGemAtRandom(currentPlayer, word.length());

        // 6. REFRESH EVERYTHING
        currentPlayer.refreshBoard();
        updateUI(); // Updates health bars and status icons

        // 7. SHOW REWARD
//        showCardPicker();
        triggerRandomRewardWheel();
    }

    private void endTurn() {
        turnTimer.stop();
        clearSelection();

        // 1. Increment the phase counter every time a turn finishes
        totalPhasesPassed++;

        // 2. Logic Change: Instead of checking for Player 2, check the counter
        // If phase is 2, 4, 6, 8... give potions
        if (totalPhasesPassed % 2 == 0) {
            System.out.println("Cycle Complete (2 Phases)! Dropping Potions...");
            giveRandomPotions();

            // Optional: Show a message on screen so players know why they got potions
            wordPreview.setText("POTION DROP!");
            wordPreview.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 24;");
        }

        // 3. Swap the current player
        currentPlayer = (currentPlayer == p1) ? p2 : p1;

        // 4. Handle Status Skip (Freeze)
        if (currentPlayer.isFrozen) {
            currentPlayer.isFrozen = false;
            endTurn();
            return;
        }

        // 5. Handle Poison
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

    // --- INVENTORY FIX ---

    private void renderInv(Player p, VBox box) {
        box.getChildren().clear();
        box.setSpacing(5);

        // 1. Render Cards (Blue Buttons)
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

        // 2. Render Potions (Green Buttons)
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
        if (t == PotionType.HEALING) p.heal(3);
        else if (t == PotionType.STRENGTH) p.hasStrength = true;
        else if (t == PotionType.PURIFY) {
            p.poisonTicks = 0; p.isPoweredDown = false; p.isFrozen = false;
        }
    }

    private void applyCard(CardType c, Player p) {
        // Determine who the opponent is
        Player enemy = (p == p1) ? p2 : p1;

        switch (c) {
        /*
           TARGETED CARDS:
           These do NOT take effect yet. They set the 'pendingCardAction'
           flag which triggers targeting mode in updateUI().
        */
            case SCRAMBLE_COL:
            case SCRAMBLE_ROW:
            case LOCKDOWN:
            case GEM_STEAL:
                pendingCardAction = c;
                // Refresh UI: This will dim the player and light up the enemy board
                updateUI();
                // We RETURN here because the card is removed only AFTER
                // the player clicks a valid target on the enemy grid.
                return;

        /*
           INSTANT CARDS:
           These happen immediately to the player or enemy without a target.
        */
            case HEALING:
                p.heal(5.0);
                System.out.println(p.name + " used Healing Card (+5 Hearts)");
                break;

            case HELP_ME:
                p.scrambles++;
                System.out.println(p.name + " used Help Me Card (+1 Scramble)");
                break;

            case SHIELD:
                p.hasShield = true;
                System.out.println(p.name + " used Graceful Shield (Invulnerable for 1 hit)");
                break;

            case SHOP_DELUXE:
                p.addPotion(PotionType.HEALING);
                p.addPotion(PotionType.STRENGTH);
                p.addPotion(PotionType.PURIFY);
                System.out.println(p.name + " used Shop Deluxe (Got 3 Potions)");
                break;

            case GEM_NUKE:
                // Wipes all gems from the opponent's board instantly
                for (int r = 0; r < 4; r++) {
                    for (int col = 0; col < 4; col++) {
                        enemy.board[r][col].setGem(null);
                    }
                }
                enemy.refreshBoard();
                System.out.println(p.name + " used Gem Nuke! Enemy gems destroyed.");
                break;
        }

        // Remove the instant card from inventory
        p.cards.remove(c);

        // Refresh UI to show the new health/potions and remove the card button
        updateUI();
    }

    // --- UTILS ---

    private void updateUI() {
        // 1. Basic Labels
        p1HealthLabel.setText(String.format("Hearts: %.1f/20.0", p1.hearts));
        p2HealthLabel.setText(String.format("Hearts: %.1f/20.0", p2.hearts));
        p1ScrambleLabel.setText("Scrambles: " + p1.scrambles);
        p2ScrambleLabel.setText("Scrambles: " + p2.scrambles);

        turnIndicator.setText(currentPlayer.name + "'s Turn");

        // 2. TARGETING MODE VISUALS
        if (pendingCardAction != null) {
            // Find the enemy
            Player enemy = (currentPlayer == p1) ? p2 : p1;

            // Light up the ENEMY grid for targeting
            enemy.gridPane.setOpacity(1.0);
            enemy.gridPane.setMouseTransparent(false);
            enemy.gridPane.setStyle("-fx-border-color: #3498db; -fx-border-width: 5; -fx-border-radius: 15;");

            // Dim the PLAYER grid (cannot spell while targeting)
            currentPlayer.gridPane.setOpacity(0.3);
            currentPlayer.gridPane.setMouseTransparent(true);

            wordPreview.setText("TARGETING: " + pendingCardAction.label);
            wordPreview.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; -fx-font-size: 28;");
        } else {
            // NORMAL TURN VISUALS
            p1GridPane.setOpacity(currentPlayer == p1 ? 1.0 : 0.4);
            p1GridPane.setMouseTransparent(currentPlayer != p1);
            p1GridPane.setStyle("-fx-border-color: #ff4757; -fx-border-width: 2; -fx-border-radius: 15;");

            p2GridPane.setOpacity(currentPlayer == p2 ? 1.0 : 0.4);
            p2GridPane.setMouseTransparent(currentPlayer != p2);
            p2GridPane.setStyle("-fx-border-color: #1e90ff; -fx-border-width: 2; -fx-border-radius: 15;");

            wordPreview.setStyle("-fx-text-fill: #ffd32a; -fx-font-weight: bold; -fx-font-size: 42;");
        }

        // 3. Render Inventories and Statuses
        renderInv(p1, p1Inventory);
        renderInv(p2, p2Inventory);
        renderStatus(p1, p1StatusBox);
        renderStatus(p2, p2StatusBox);
    }

    private void renderStatus(Player p, VBox box) {
        box.getChildren().clear();
        box.setSpacing(5);

        // Order of importance
        if (p.isFrozen) {
            box.getChildren().add(createStatusIndicator("❄ FROZEN", "#2980b9"));
        }
        if (p.poisonTicks > 0) {
            box.getChildren().add(createStatusIndicator("☣ POISON (" + p.poisonTicks + "T)", "#8e44ad"));
        }

        // Check this specific boolean
        if (p.isPoweredDown) {
            box.getChildren().add(createStatusIndicator("↓ POWER DOWN", "#d35400"));
        }

        if (p.hasStrength) {
            box.getChildren().add(createStatusIndicator("↑ STRENGTH UP", "#27ae60"));
        }
        if (p.hasShield) {
            box.getChildren().add(createStatusIndicator("🛡 SHIELDED", "#16a085"));
        }
    }

    // Helper to keep styling consistent
    private Button createStatusIndicator(String text, String color) {
        Button b = new Button(text);
        b.setDisable(true); // Ensures user cannot click it
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-opacity: 1.0; " +
                "-fx-font-weight: bold; -fx-pref-width: 160; -fx-font-size: 11px; -fx-border-color: white; -fx-border-width: 0.5;");
        return b;
    }

    private void checkGameOver() {
        turnTimer.stop();
        String winner = p1.hearts > 0 ? "Player 1" : "Player 2";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(winner + " Wins!");
        alert.setContentText("Play again?");

        ButtonType btnYes = new ButtonType("Yes");
        ButtonType btnNo = new ButtonType("No, Hub");
        alert.getButtonTypes().setAll(btnYes, btnNo);

        alert.showAndWait().ifPresent(type -> {
            if (type == btnYes) startCountdown();
            else quitToLobby();
        });
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

            if (timerLabel != null) {
                timerLabel.setText("Time: " + secondsRemaining + "s");
            }

            if (secondsRemaining <= 0) {
                System.out.println("Time up! Skipping turn.");
                onSkip();
            }
        }));
        turnTimer.setCycleCount(Timeline.INDEFINITE);
    }

    private void startTurnTimer() {
        secondsRemaining = 30;
        if (timerLabel != null) {
            timerLabel.setText("Time: 30s");
        }
        turnTimer.playFromStart();
    }

    private void handleMouseClick(javafx.scene.input.MouseEvent e, Player p, Tile tile) {
        if (pendingCardAction != null) {
            // We only care if they clicked the ENEMY'S board
            Player enemy = (currentPlayer == p1) ? p2 : p1;

            if (p == enemy) {
                // 1. Execute the effect on this specific tile
                executeTargetedCard(pendingCardAction, enemy, tile);

                // 2. Remove the card from the USER
                currentPlayer.cards.remove(pendingCardAction);

                // 3. Reset the targeting state
                pendingCardAction = null;

                // 4. Swap visuals back to normal so player can spell their word
                updateUI();
            }
            return;
        }

        // Normal word selection (Left click) or Clear (Right click)
        if (e.getButton() == MouseButton.SECONDARY) {
            onClear();
        } else {
            handleTileClick(p, tile);
        }
    }

    private void executeTargetedCard(CardType type, Player enemy, Tile targetTile) {
        Random r = new Random();
        switch (type) {
            case LOCKDOWN -> targetTile.isLocked = true;
            case SCRAMBLE_ROW -> {
                for (int i = 0; i < 4; i++) {
                    if (enemy.board[targetTile.r][i].gemType == null)
                        enemy.board[targetTile.r][i].letter = "JKXYZQ".charAt(r.nextInt(6));
                }
            }
            case SCRAMBLE_COL -> {
                for (int i = 0; i < 4; i++) {
                    if (enemy.board[i][targetTile.c].gemType == null)
                        enemy.board[i][targetTile.c].letter = "JKXYZQ".charAt(r.nextInt(6));
                }
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
        if (len == 5) type = GemType.POISON;      // 5 Letters -> Purple
        else if (len == 6) type = GemType.HEAL;    // 6 Letters -> Green
        else if (len == 7) type = GemType.POWER_DOWN; // 7 Letters -> Orange
        else if (len >= 8) type = GemType.FREEZE;  // 8+ Letters -> Blue

        if (type != null) {
            List<Tile> availableTiles = new ArrayList<>();
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    Tile t = p.board[r][c];
                    // Only spawn on tiles that are NOT already gems
                    if (t != null && t.gemType == null) {
                        availableTiles.add(t);
                    }
                }
            }

            if (!availableTiles.isEmpty()) {
                Tile target = availableTiles.get(random.nextInt(availableTiles.size()));
                target.setGem(type); // This calls updateVisuals()
                System.out.println("UI UPDATE: " + type + " gem spawned at " + target.r + "," + target.c);
            }
        }
    }

    private void applyGemEffect(GemType type, Player enemy) {
        switch (type) {
            case POISON -> {
                enemy.poisonTicks = 2; // Purple
            }
            case HEAL -> {
                currentPlayer.heal(2.5); // Green (Direct effect, no status needed)
            }
            case POWER_DOWN -> {
                enemy.isPoweredDown = true; // Orange
            }
            case FREEZE -> {
                enemy.isFrozen = true; // Blue
            }
        }
    }

    @FXML
    private void onScramble() {
        if (currentPlayer.scrambles > 0) {
            onClear(); // Reset word
            currentPlayer.scrambles--; // Decrement charge

            setupBoard(currentPlayer); // Shuffle non-gem tiles
            updateUI(); // Force label to update from 2 -> 1 -> 0
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
        clearSelection();
    }

    private void clearSelection() {
        // We clear BOTH players' potential selections just to be safe during turn transitions
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

    private void showCardPicker() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #2c3e50;");

        Label l = new Label(currentPlayer.name + " Choose a Reward");
        l.setTextFill(Color.WHITE);
        box.getChildren().add(l);

        List<CardType> pool = new ArrayList<>(Arrays.asList(CardType.values()));
        Collections.shuffle(pool);

        {
            CardType card = pool.get(0);
            Button b = new Button(card.label);
            b.setPrefWidth(200);
            b.setOnAction(e -> {
                currentPlayer.addCard(card);
                stage.close();
                endTurn();
            });
            box.getChildren().add(b);
        }
        {
            CardType card = pool.get(1);
            Button b = new Button(card.label);
            b.setPrefWidth(200);
            b.setOnAction(e -> {
                currentPlayer.addCard(card);
                stage.close();
                endTurn();
            });
            box.getChildren().add(b);
        }
        {
            CardType card = pool.get(2);
            Button b = new Button(card.label);
            b.setPrefWidth(200);
            b.setOnAction(e -> {
                currentPlayer.addCard(card);
                stage.close();
                endTurn();
            });
            box.getChildren().add(b);
        }

        stage.setScene(new Scene(box));
        stage.show();
    }

    private void triggerRandomRewardWheel() {
        wheelOverlay.setVisible(true);
        gameUI.setDisable(true);

        CardType[] allCards = CardType.values();
        CardType winningCard = allCards[random.nextInt(allCards.length)];

        // Animation Settings
        final int totalFlickers = 15; // How many times it changes before stopping
        final int[] currentFlicker = {0};
        final double[] currentDelay = {0.05}; // Start very fast (50ms)

        // We use a recursive Timeline approach to simulate slowing down
        runFlickerStep(allCards, winningCard, totalFlickers, currentFlicker, currentDelay);
    }

    private void runFlickerStep(CardType[] allCards, CardType winningCard, int total, int[] current, double[] delay) {
        Timeline flicker = new Timeline(new KeyFrame(Duration.seconds(delay[0]), e -> {
            current[0]++;

            if (current[0] < total) {
                // Pick a random card to show during the flicker (not necessarily the winner)
                wheelStatusLabel.setText(allCards[random.nextInt(allCards.length)].label);

                // Slow down the next flicker by 15%
                delay[0] *= 1.15;

                // Repeat
                runFlickerStep(allCards, winningCard, total, current, delay);
            } else {
                // STOPPED! Reveal the actual winner
                revealFinalReward(winningCard);
            }
        }));
        flicker.play();
    }

    private void revealFinalReward(CardType winner) {
        wheelStatusLabel.setText(winner.label);
        wheelStatusLabel.setTextFill(Color.web("#ffd32a")); // Gold highlight for winner
        wheelResultLabel.setText("REWARD GRANTED: " + winner.label);

        // Pause so they can see it, then give the card and close
        Timeline pause = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            currentPlayer.addCard(winner);
            wheelStatusLabel.setTextFill(Color.WHITE); // Reset for next time
            wheelOverlay.setVisible(false);
            gameUI.setDisable(false);
            endTurn(); // Transition to next player
        }));
        pause.play();
    }

    private void giveRandomPotions() {
        PotionType[] pts = PotionType.values();
        Random r = new Random();
        p1.addPotion(pts[r.nextInt(pts.length)]);
        p2.addPotion(pts[r.nextInt(pts.length)]);
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
        // 1. Mark selected tiles as empty
        for (Tile t : p.selectedTiles) {
            t.letter = ' ';
            t.gemType = null; // Important: ensure gems are cleared when used
            t.setSelected(false);
        }
        p.selectedTiles.clear();

        // 2. Physics logic
        for (int c = 0; c < 4; c++) {
            for (int r = 3; r >= 0; r--) {
                if (p.board[r][c].letter == ' ') {
                    for (int k = r - 1; k >= 0; k--) {
                        if (p.board[k][c].letter != ' ') {
                            // Move letter AND gemType down
                            p.board[r][c].letter = p.board[k][c].letter;
                            p.board[r][c].gemType = p.board[k][c].gemType;

                            p.board[k][c].letter = ' ';
                            p.board[k][c].gemType = null;
                            break;
                        }
                    }
                }
            }
            // 3. Fill top with new letters
            for (int r = 0; r < 4; r++) {
                if (p.board[r][c].letter == ' ') {
                    p.board[r][c].letter = generateLetter();
                    p.board[r][c].gemType = null;
                }
            }
        }
        p.refreshBoard();
    }

    private char generateLetter() { return DATA.charAt(new Random().nextInt(DATA.length())); }
}

