package com.capstone.javabox.csit228.games.chainreaction;

import com.capstone.javabox.csit228.games.JavaboxAbstractController;
import com.capstone.javabox.csit228.utils.SoundManager;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.*;

public class ChainReactionController extends JavaboxAbstractController {

    // --- Scenes ---
    @FXML private StackPane rootPane;
    @FXML private VBox setupScene;
    @FXML private VBox gameScene;
    @FXML private VBox winScene;

    // --- Setup ---
    @FXML private ToggleGroup playerCountGroup;
    @FXML private ToggleGroup gridSizeGroup;
    @FXML private TextField p1Name, p2Name, p3Name, p4Name;

    // --- Game ---
    @FXML private GridPane gameGrid;
    @FXML private Label turnLabel;
    @FXML private Label statusLabel;
    @FXML private HBox playerIndicators;

    // --- Win ---
    @FXML private Label winLabel;
    @FXML private Label winSubLabel;

    // --- Game State ---
    private ChainReactionGame game;
    private int playerCount;
    private int gridSize;
    private String[] playerNames;
    private boolean[] eliminated;
    private boolean[] hasPlaced;
    private int currentPlayer;
    private boolean animating;
    private boolean chainOccurred;

    // Stores the waves from the last move — passed to infection phase
    private List<ChainReactionGame.ExplosionWave> lastWaves = new ArrayList<>();

    // Cells currently showing plague icon — cleared at start of each turn
    private final Set<String> infectedCellKeys = new HashSet<>();

    private static final String[] PLAYER_COLORS = {
            "#e94560", "#4f9ef5", "#4caf50", "#f5a623"
    };
    private static final String[] PLAYER_COLOR_NAMES = {
            "Red", "Blue", "Green", "Yellow"
    };

    private StackPane[][] cellPanes;

    @FXML
    public void initialize() {
        showScene(setupScene);
    }

    // --- Setup ---

    @FXML
    private void handleStartGame() {
        RadioButton selectedCount = (RadioButton) playerCountGroup.getSelectedToggle();
        if (selectedCount == null) {
            statusLabel.setText("⚠ Please select player count!");
            return;
        }
        playerCount = Integer.parseInt(selectedCount.getText());

        RadioButton selectedSize = (RadioButton) gridSizeGroup.getSelectedToggle();
        if (selectedSize == null) {
            statusLabel.setText("⚠ Please select grid size!");
            return;
        }
        gridSize = selectedSize.getText().equals("9×9") ? 9 : 16;

        TextField[] nameFields = {p1Name, p2Name, p3Name, p4Name};
        playerNames = new String[playerCount];
        for (int i = 0; i < playerCount; i++) {
            String name = nameFields[i].getText().trim();
            playerNames[i] = name.isEmpty() ? "Player " + (i + 1) : name;
        }

        startGame();
    }

    private void startGame() {
        game = new ChainReactionGame(gridSize, gridSize, playerCount);
        eliminated = new boolean[playerCount];
        hasPlaced = new boolean[playerCount];
        currentPlayer = 0;
        animating = false;
        chainOccurred = false;
        lastWaves = new ArrayList<>();
        infectedCellKeys.clear();

        buildGameGrid();
        buildPlayerIndicators();
        updateTurnLabel();
        statusLabel.setText("");

        showScene(gameScene);
    }

    // --- Grid Building ---

    private void buildGameGrid() {
        gameGrid.getChildren().clear();
        gameGrid.setHgap(2);
        gameGrid.setVgap(2);
        cellPanes = new StackPane[gridSize][gridSize];

        double cellSize = gridSize == 9 ? 72 : 42;

        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                StackPane pane = new StackPane();
                pane.setPrefSize(cellSize, cellSize);
                pane.setStyle(emptyCellStyle(game.getBoard()[r][c].getType()));

                final int fr = r, fc = c;
                pane.setOnMouseClicked(e -> handleCellClick(fr, fc));

                cellPanes[r][c] = pane;
                gameGrid.add(pane, c, r);
            }
        }
    }

    private void buildPlayerIndicators() {
        playerIndicators.getChildren().clear();
        playerIndicators.setSpacing(16);

        for (int i = 0; i < playerCount; i++) {
            Label indicator = new Label(playerNames[i] + " (" + PLAYER_COLOR_NAMES[i] + ")");
            indicator.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
            indicator.setTextFill(Color.web(PLAYER_COLORS[i]));
            indicator.setPadding(new Insets(4, 10, 4, 10));
            indicator.setStyle("-fx-border-color: " + PLAYER_COLORS[i] + "; " +
                    "-fx-border-radius: 4; -fx-background-radius: 4; " +
                    "-fx-background-color: transparent;");
            indicator.setId("indicator_" + i);
            playerIndicators.getChildren().add(indicator);
        }
    }

    // --- Cell Click ---

    private void handleCellClick(int row, int col) {
        if (animating) return;
        if (eliminated[currentPlayer]) { advanceTurn(); return; }

        Cell cell = game.getBoard()[row][col];
        if (!cell.isEmpty() && cell.getOwner() != currentPlayer) return;

        SoundManager.playSFX("sfx_ui_confirm.mp3");
        hasPlaced[currentPlayer] = true;
        animating = true;

        // Clear plague icons from previous turn
        infectedCellKeys.clear();

        lastWaves = game.placeOrb(row, col, currentPlayer);
        chainOccurred = !lastWaves.isEmpty();

        updateCellUI(row, col);

        if (!chainOccurred) {
            // No explosion — no chain, no infection, just advance
            finishTurn();
        } else {
            animateWaves(lastWaves, 0, this::runInfectionPhase);
        }
    }

    // --- Wave Animation (flash-based) ---

    private void animateWaves(List<ChainReactionGame.ExplosionWave> waves, int waveIndex, Runnable onDone) {
        if (waveIndex >= waves.size()) {
            onDone.run();
            return;
        }

        ChainReactionGame.ExplosionWave wave = waves.get(waveIndex);

        // Collect all cells to update after this wave
        Set<String> toUpdate = new LinkedHashSet<>();
        for (int[] pos : wave.explodingCells) {
            toUpdate.add(pos[0] + "," + pos[1]);
            for (int[] neighbor : game.getNeighbors(pos[0], pos[1])) {
                toUpdate.add(neighbor[0] + "," + neighbor[1]);
            }
        }

        // Flash exploding cells white
        for (int[] pos : wave.explodingCells) {
            cellPanes[pos[0]][pos[1]].setStyle(
                    "-fx-background-color: white; -fx-border-color: white; " +
                            "-fx-border-radius: 3; -fx-background-radius: 3;"
            );
        }

        SoundManager.playSFX("sfx_ui_confirm.mp3");

        // After 150ms update all affected cells, then 80ms pause before next wave
        Timeline flashTimer = new Timeline(new KeyFrame(Duration.millis(150), e -> {
            for (String key : toUpdate) {
                String[] parts = key.split(",");
                updateCellUI(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }
            Timeline pauseTimer = new Timeline(new KeyFrame(Duration.millis(80), ev ->
                    animateWaves(waves, waveIndex + 1, onDone)
            ));
            pauseTimer.play();
        }));
        flashTimer.play();
    }

    // --- Infection Phase (chain-cells only) ---

    private void runInfectionPhase() {
        // Only infect cells involved in the chain
        ChainReactionGame.InfectionResult result = game.runInfectionPhase(lastWaves);

        if (result.infectedCells.isEmpty()) {
            finishTurn();
            return;
        }

        // Show plague icons on spreading cells
        for (int[] cell : result.infectedCells)
            infectedCellKeys.add(cell[0] + "," + cell[1]);
        for (int[] cell : result.infectedCells)
            updateCellUI(cell[0], cell[1]);

        // After 400ms apply conversion/death results
        Timeline delay = new Timeline(new KeyFrame(Duration.millis(400), e -> {
            for (int[] cell : result.convertedCells) updateCellUI(cell[0], cell[1]);
            for (int[] cell : result.diedCells)      updateCellUI(cell[0], cell[1]);

            if (!result.convertedCells.isEmpty() || !result.diedCells.isEmpty()) {
                statusLabel.setText("☣ Infection! " +
                        result.convertedCells.size() + " converted, " +
                        result.diedCells.size() + " destroyed.");
            }

            finishTurn();
        }));
        delay.play();
    }

    private void finishTurn() {
        checkEliminationsAndAdvance();
    }

    // --- Elimination & Turn ---

    private void checkEliminationsAndAdvance() {
        boolean allHavePlaced = true;
        for (boolean p : hasPlaced) if (!p) { allHavePlaced = false; break; }

        if (allHavePlaced) {
            for (int i = 0; i < playerCount; i++) {
                if (!eliminated[i] && game.isEliminated(i, true)) {
                    eliminated[i] = true;
                    statusLabel.setText("💀 " + playerNames[i] + " has been eliminated!");
                    SoundManager.playSFX("sfx_death.mp3");
                    dimPlayerIndicator(i);
                }
            }
        }

        int survivors = 0;
        int lastSurvivor = -1;
        for (int i = 0; i < playerCount; i++) {
            if (!eliminated[i]) { survivors++; lastSurvivor = i; }
        }

        if (survivors == 1) {
            showWin(lastSurvivor);
            return;
        }

        advanceTurn();
    }

    private void advanceTurn() {
        do {
            currentPlayer = (currentPlayer + 1) % playerCount;
        } while (eliminated[currentPlayer]);

        animating = false;
        chainOccurred = false;
        updateTurnLabel();
        highlightCurrentPlayerIndicator();
    }

    private void updateTurnLabel() {
        turnLabel.setText(playerNames[currentPlayer] + "'s Turn");
        turnLabel.setStyle("-fx-font-family: Monospace; -fx-font-size: 18; " +
                "-fx-text-fill: " + PLAYER_COLORS[currentPlayer] + ";");
    }

    private void dimPlayerIndicator(int playerIndex) {
        for (javafx.scene.Node node : playerIndicators.getChildren()) {
            if (("indicator_" + playerIndex).equals(node.getId())) {
                node.setOpacity(0.3);
            }
        }
    }

    private void highlightCurrentPlayerIndicator() {
        for (int i = 0; i < playerIndicators.getChildren().size(); i++) {
            javafx.scene.Node node = playerIndicators.getChildren().get(i);
            if (!eliminated[i]) {
                node.setOpacity(i == currentPlayer ? 1.0 : 0.5);
            }
        }
    }

    // --- Win ---

    private void showWin(int playerIndex) {
        animating = false;
        winLabel.setText("🏆 " + playerNames[playerIndex] + " Wins!");
        winLabel.setStyle("-fx-font-family: Monospace; -fx-font-size: 28; " +
                "-fx-text-fill: " + PLAYER_COLORS[playerIndex] + ";");
        winSubLabel.setText(PLAYER_COLOR_NAMES[playerIndex] + " dominates the board!");
        winSubLabel.setStyle("-fx-font-family: Monospace; -fx-font-size: 14; " +
                "-fx-text-fill: #aaaaaa;");
        SoundManager.playSFX("sfx_powerup.mp3");
        showScene(winScene);
    }

    @FXML
    private void handlePlayAgain() {
        SoundManager.playSFX("sfx_ui_confirm.mp3");
        showScene(setupScene);
    }

    @FXML
    private void handleQuit() {
        SoundManager.playSFX("sfx_ui_accept_death.mp3");
        quitToLobby();
    }

    // --- Cell UI ---

    private void updateCellUI(int r, int c) {
        Cell cell = game.getBoard()[r][c];
        StackPane pane = cellPanes[r][c];
        pane.getChildren().clear();

        if (cell.isEmpty()) {
            pane.setStyle(emptyCellStyle(cell.getType()));
            return;
        }

        String color = PLAYER_COLORS[cell.getOwner()];
        pane.setStyle(filledCellStyle(color, cell.getType()));

        // Orb circles
        HBox orbBox = new HBox(3);
        orbBox.setAlignment(Pos.CENTER);

        int count = Math.min(cell.getOrbCount(), 6);
        for (int i = 0; i < count; i++) {
            Circle orb = new Circle(gridSize == 9 ? 7 : 4);
            orb.setFill(Color.web(color));
            orb.setEffect(new javafx.scene.effect.Glow(0.6));
            orbBox.getChildren().add(orb);
        }

        if (cell.getOrbCount() > 6) {
            Label countLabel = new Label("+" + (cell.getOrbCount() - 6));
            countLabel.setFont(Font.font("Monospace", 9));
            countLabel.setTextFill(Color.web(color));
            orbBox.getChildren().add(countLabel);
        }

        pane.getChildren().add(orbBox);

        // Special cell icon — top right
        if (cell.getType() == Cell.CellType.FORTIFIED) {
            Label icon = new Label("🛡");
            icon.setFont(Font.font(9));
            StackPane.setAlignment(icon, Pos.TOP_RIGHT);
            pane.getChildren().add(icon);
        } else if (cell.getType() == Cell.CellType.MULTIPLIER) {
            Label icon = new Label("✕");
            icon.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
            icon.setTextFill(Color.web("#f5a623"));
            StackPane.setAlignment(icon, Pos.TOP_RIGHT);
            pane.getChildren().add(icon);
        }

        // Plague icon — bottom left, only during infection phase of this turn
        if (infectedCellKeys.contains(r + "," + c)) {
            Label plague = new Label("☣");
            plague.setFont(Font.font(gridSize == 9 ? 12 : 8));
            plague.setTextFill(Color.web("#39ff14"));
            StackPane.setAlignment(plague, Pos.BOTTOM_LEFT);
            pane.getChildren().add(plague);
        }
    }

    // --- Scene Switching ---

    private void showScene(VBox scene) {
        for (VBox s : new VBox[]{setupScene, gameScene, winScene}) {
            s.setVisible(false);
            s.setManaged(false);
        }
        scene.setVisible(true);
        scene.setManaged(true);
    }

    // --- Styles ---

    private String emptyCellStyle(Cell.CellType type) {
        String border = type == Cell.CellType.FORTIFIED ? "#4f9ef5" :
                type == Cell.CellType.MULTIPLIER ? "#f5a623" : "#2a2a4a";
        return "-fx-background-color: #16213e; -fx-border-color: " + border + "; " +
                "-fx-border-width: " + (type == Cell.CellType.NORMAL ? "1" : "2") + "; " +
                "-fx-border-radius: 3; -fx-background-radius: 3;";
    }

    private String filledCellStyle(String color, Cell.CellType type) {
        String border = type == Cell.CellType.FORTIFIED ? "#4f9ef5" :
                type == Cell.CellType.MULTIPLIER ? "#f5a623" : color;
        return "-fx-background-color: #0d0d1a; -fx-border-color: " + border + "; " +
                "-fx-border-width: " + (type == Cell.CellType.NORMAL ? "1" : "2") + "; " +
                "-fx-border-radius: 3; -fx-background-radius: 3;";
    }
}