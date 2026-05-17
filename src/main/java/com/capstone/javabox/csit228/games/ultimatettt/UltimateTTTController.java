package com.capstone.javabox.csit228.games.ultimatettt;

import com.capstone.javabox.csit228.games.JavaboxAbstractController;
import com.capstone.javabox.csit228.database.UTTTDAO;
import com.capstone.javabox.csit228.utils.SoundManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class UltimateTTTController extends JavaboxAbstractController {

    @FXML private GridPane mainBoard;
    @FXML private Label turnLabel;
    @FXML private Label statusLabel;
    @FXML private HBox p1Hand;
    @FXML private HBox p2Hand;
    @FXML private Label p1Label;
    @FXML private Label p2Label;
    @FXML private Button restartButton;
    @FXML private VBox savePane;
    @FXML private TextField p1NameInput;
    @FXML private TextField p2NameInput;
    @FXML private Button saveScoreBtn;
    private char matchWinner = '\0';

    private static final int SIZE = 3;
    private static final Random random = new Random();

    // Board state
    // smallBoard[br][bc][sr][sc] = 'X', 'O', '#' (blocked), or '\0'
    private char[][][][] smallBoard;
    private char[][] bigBoard; // 'X', 'O', or '\0'
    private boolean[][][][] anchored; // anchored cells

    // Game state
    private char currentPlayer; // 'X' or 'O'
    private int forcedBigRow = -1; // -1 means free choice
    private int forcedBigCol = -1;
    private boolean gameOver;
    private boolean skipNextTurn;
    private boolean doubleTurn;
    private boolean cardUsedThisTurn;
    private Card lastCardPlayed;

    // Card state
    private List<Card> p1Cards;
    private List<Card> p2Cards;
    private Card selectedCard;
    private Button selectedCardButton;

    // UI references
    private Button[][][][] cellButtons;   // [bigRow][bigCol][smallRow][smallCol]
    private StackPane[][] bigPanes;       // [bigRow][bigCol]

    @FXML
    public void initialize() {
        newGame();
    }

    private void newGame() {
        SoundManager.playMusic("music_TTT_p1.mp3");
        smallBoard = new char[SIZE][SIZE][SIZE][SIZE];
        bigBoard = new char[SIZE][SIZE];
        anchored = new boolean[SIZE][SIZE][SIZE][SIZE];
        currentPlayer = 'X';
        forcedBigRow = -1;
        forcedBigCol = -1;
        gameOver = false;
        skipNextTurn = false;
        doubleTurn = false;
        cardUsedThisTurn = false;
        lastCardPlayed = null;
        selectedCard = null;
        selectedCardButton = null;

        p1Cards = CardDeck.drawHand(3);
        p2Cards = CardDeck.drawHand(3);

        cellButtons = new Button[SIZE][SIZE][SIZE][SIZE];
        bigPanes = new StackPane[SIZE][SIZE];

        buildBoard();
        updateHandUI();
        updateTurnLabel();
        statusLabel.setText("");
        restartButton.setVisible(false);
        if (savePane != null) {
            savePane.setVisible(false);
            savePane.setManaged(false);
            p1NameInput.setDisable(false);
            p2NameInput.setDisable(false);
            p1NameInput.clear();
            p2NameInput.clear();
            saveScoreBtn.setDisable(false);
            saveScoreBtn.setText("Upload Match");
        }
        matchWinner = '\0';
    }

    // --- Board Building ---

    private void buildBoard() {
        mainBoard.getChildren().clear();
        mainBoard.setHgap(6);
        mainBoard.setVgap(6);

        for (int br = 0; br < SIZE; br++) {
            for (int bc = 0; bc < SIZE; bc++) {
                StackPane bigPane = buildBigCell(br, bc);
                bigPanes[br][bc] = bigPane;
                mainBoard.add(bigPane, bc, br);
            }
        }
        highlightActiveBoards();
    }

    private StackPane buildBigCell(int br, int bc) {
        StackPane stack = new StackPane();
        stack.setMinSize(150, 150);
        stack.setStyle(bigCellStyle(false));

        GridPane smallGrid = new GridPane();
        smallGrid.setHgap(3);
        smallGrid.setVgap(3);
        smallGrid.setAlignment(Pos.CENTER);
        smallGrid.setPadding(new Insets(6));

        for (int sr = 0; sr < SIZE; sr++) {
            for (int sc = 0; sc < SIZE; sc++) {
                Button btn = new Button();
                btn.setPrefSize(40, 40);
                btn.setStyle(emptyCellStyle());

                final int fbr = br, fbc = bc, fsr = sr, fsc = sc;
                btn.setOnAction(e -> handleCellClick(fbr, fbc, fsr, fsc));

                cellButtons[br][bc][sr][sc] = btn;
                smallGrid.add(btn, sc, sr);
            }
        }

        stack.getChildren().add(smallGrid);
        return stack;
    }

    // --- Cell Click Handler ---

    private void handleCellClick(int br, int bc, int sr, int sc) {
        if (gameOver) return;

        // If a card is selected and needs a target
        if (selectedCard != null) {
            handleCardTarget(br, bc, sr, sc);
            return;
        }

        // Normal move
        if (!isBoardActive(br, bc)) return;
        if (smallBoard[br][bc][sr][sc] != '\0') return;
        if (bigBoard[br][bc] != '\0') return;

        placeSymbol(br, bc, sr, sc, currentPlayer);
    }

    private void placeSymbol(int br, int bc, int sr, int sc, char symbol) {
        SoundManager.playSFX("sfx_collect.mp3");
        smallBoard[br][bc][sr][sc] = symbol;
        updateCellUI(br, bc, sr, sc);

        // Check small board win
        char smallWinner = checkWinner(smallBoard[br][bc]);
        if (smallWinner != '\0') {
            bigBoard[br][bc] = smallWinner;
            showBigCellClaim(br, bc, smallWinner);
        } else if (isBoardFull(smallBoard[br][bc])) {
            bigBoard[br][bc] = 'D'; // Draw
            showBigCellClaim(br, bc, 'D');
        }

        // Check big board win
        char bigWinner = checkWinner(bigBoard);
        if (bigWinner != '\0') {
            endGame(bigWinner);
            return;
        }

        if (isAllBoardsFull()) {
            endGame('D');
            return;
        }

        // Next forced board = position of cell just played
        if (bigBoard[sr][sc] == '\0') {
            forcedBigRow = sr;
            forcedBigCol = sc;
        } else {
            forcedBigRow = -1;
            forcedBigCol = -1;
        }

        advanceTurn();
    }

    private void advanceTurn() {
        if (doubleTurn) {
            doubleTurn = false;
            cardUsedThisTurn = false;
            highlightActiveBoards();
            updateTurnLabel();
            return;
        }

        // Switch player
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
        cardUsedThisTurn = false;

        // Draw a card if hand has fewer than 3
        List<Card> hand = getCurrentHand();
        if (hand.size() < 3) hand.add(CardDeck.drawCard());
        updateHandUI();

        if (skipNextTurn) {
            skipNextTurn = false;
            statusLabel.setText("⏭ " + currentPlayer + "'s turn was skipped!");
            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
            if (getCurrentHand().size() < 3) getCurrentHand().add(CardDeck.drawCard());
            updateHandUI();
        }

        highlightActiveBoards();
        updateTurnLabel();

        if (currentPlayer == 'X') {
            SoundManager.playMusic("music_TTT_p1.mp3");
        } else {
            SoundManager.playMusic("music_TTT_p2.mp3");
        }
    }

    // --- Card UI ---

    private void updateHandUI() {
        renderHand(p1Hand, p1Cards, 'X');
        renderHand(p2Hand, p2Cards, 'O');
    }

    private void renderHand(HBox handBox, List<Card> cards, char player) {
        handBox.getChildren().clear();
        handBox.setSpacing(8);
        handBox.setAlignment(Pos.CENTER);

        for (Card card : cards) {
            Button cardBtn = new Button(card.getDisplayName());
            cardBtn.setWrapText(true);
            cardBtn.setPrefSize(110, 70);
            cardBtn.setStyle(cardStyle(card, false));
            cardBtn.setTooltip(new javafx.scene.control.Tooltip(card.getDescription()));

            cardBtn.setOnAction(e -> handleCardClick(card, cardBtn, player));
            handBox.getChildren().add(cardBtn);
        }
    }

    private void handleCardClick(Card card, Button cardBtn, char player) {
        if (gameOver) return;
        if (player != currentPlayer) return;
        if (cardUsedThisTurn) {
            statusLabel.setText("⚠ Already used a card this turn!");
            return;
        }

        // Deselect if clicking same card again
        if (selectedCard == card) {
            selectedCard = null;
            selectedCardButton.setStyle(cardStyle(card, false));
            selectedCardButton = null;
            statusLabel.setText("");
            return;
        }

        // Deselect previous
        if (selectedCardButton != null) {
            selectedCardButton.setStyle(cardStyle(selectedCard, false));
        }

        selectedCard = card;
        selectedCardButton = cardBtn;
        cardBtn.setStyle(cardStyle(card, true));

        if (card.getTargetType() == Card.TargetType.INSTANT) {
            activateCard(card, -1, -1, -1, -1);
        } else if (card.getTargetType() == Card.TargetType.TARGET_SMALL_BOARD) {
            statusLabel.setText("🎯 Click a big board to target it.");
        } else {
            statusLabel.setText("🎯 Click a cell to target it.");
        }
    }

    private void handleCardTarget(int br, int bc, int sr, int sc) {
        if (selectedCard.getTargetType() == Card.TargetType.TARGET_SMALL_BOARD) {
            activateCard(selectedCard, br, bc, -1, -1);
        } else if (selectedCard.getTargetType() == Card.TargetType.TARGET_CELL) {
            activateCard(selectedCard, br, bc, sr, sc);
        }
    }

    // --- Card Activation ---

    private void activateCard(Card card, int br, int bc, int sr, int sc) {
        List<Card> hand = getCurrentHand();
        char opponent = (currentPlayer == 'X') ? 'O' : 'X';

        switch (card.getType()) {

            case SKIP_ENEMY_TURN -> {
                skipNextTurn = true;
                statusLabel.setText("⏭ Enemy turn will be skipped!");
                SoundManager.playSFX("sfx_ui_confirm.mp3");
            }

            case DELETE_1_ENEMY -> {
                if (sr == -1) { statusLabel.setText("🎯 Click an enemy cell."); return; }
                if (smallBoard[br][bc][sr][sc] != opponent) {
                    statusLabel.setText("⚠ That's not an enemy cell!");
                    return;
                }
                if (anchored[br][bc][sr][sc]) {
                    statusLabel.setText("⚠ That cell is anchored!");
                    return;
                }
                smallBoard[br][bc][sr][sc] = '\0';
                updateCellUI(br, bc, sr, sc);
                statusLabel.setText("🗑 Deleted one enemy symbol!");
                SoundManager.playSFX("sfx_explosion.mp3");
            }

            case DELETE_2_ENEMY -> {
                List<int[]> enemyCells = findCells(opponent, false);
                Collections.shuffle(enemyCells);
                int deleted = 0;
                for (int[] cell : enemyCells) {
                    if (deleted >= 2) break;
                    if (!anchored[cell[0]][cell[1]][cell[2]][cell[3]]) {
                        smallBoard[cell[0]][cell[1]][cell[2]][cell[3]] = '\0';
                        updateCellUI(cell[0], cell[1], cell[2], cell[3]);
                        deleted++;
                    }
                }
                statusLabel.setText("🗑 Deleted " + deleted + " enemy symbols!");
                SoundManager.playSFX("sfx_explosion_long.mp3");
            }

            case SUMMON_ALLY -> {
                List<int[]> emptyCells = findEmptyCells();
                if (!emptyCells.isEmpty()) {
                    int[] cell = emptyCells.get(random.nextInt(emptyCells.size()));
                    smallBoard[cell[0]][cell[1]][cell[2]][cell[3]] = currentPlayer;
                    updateCellUI(cell[0], cell[1], cell[2], cell[3]);
                    // Check if this wins a small board
                    char sw = checkWinner(smallBoard[cell[0]][cell[1]]);
                    if (sw != '\0') {
                        bigBoard[cell[0]][cell[1]] = sw;
                        showBigCellClaim(cell[0], cell[1], sw);
                    }
                    statusLabel.setText("✨ Summoned an ally!");
                    SoundManager.playSFX("sfx_ui_confirm.mp3");
                } else {
                    statusLabel.setText("⚠ No empty cells!");
                }
            }

            case BLOCK_CELL -> {
                if (sr == -1) { statusLabel.setText("🎯 Click an empty cell."); return; }
                if (smallBoard[br][bc][sr][sc] != '\0') {
                    statusLabel.setText("⚠ Cell is not empty!");
                    return;
                }
                smallBoard[br][bc][sr][sc] = '#';
                updateCellUI(br, bc, sr, sc);
                statusLabel.setText("🚫 Cell blocked!");
                SoundManager.playSFX("sfx_ui_confirm.mp3");
            }

            case STEAL -> {
                List<Card> opponentHand = getHandFor(opponent);
                if (opponentHand.isEmpty()) {
                    statusLabel.setText("⚠ Opponent has no cards!");
                    return;
                }
                Card stolen = opponentHand.remove(random.nextInt(opponentHand.size()));
                hand.add(stolen);
                statusLabel.setText("🤏 Stole: " + stolen.getDisplayName() + "!");
                SoundManager.playSFX("sfx_ui_confirm.mp3");
            }

            case REDRAW -> {
                hand.clear();
                hand.addAll(CardDeck.drawHand(3));
                statusLabel.setText("🔄 Redrawn 3 new cards!");
                SoundManager.playSFX("sfx_ui_confirm.mp3");
            }

            case ANCHOR -> {
                if (sr == -1) { statusLabel.setText("🎯 Click your own cell to anchor."); return; }
                if (smallBoard[br][bc][sr][sc] != currentPlayer) {
                    statusLabel.setText("⚠ You can only anchor your own cells!");
                    return;
                }
                anchored[br][bc][sr][sc] = true;
                updateCellUI(br, bc, sr, sc);
                statusLabel.setText("⚓ Cell anchored — immune to Nuke and Delete!");
                SoundManager.playSFX("sfx_ui_confirm.mp3");
            }

            case DOUBLE_TURN -> {
                doubleTurn = true;
                statusLabel.setText("⚡ Double turn! Play again after this move.");
                SoundManager.playSFX("sfx_ui_confirm.mp3");
            }

            case NUKE -> {
                if (br == -1) { statusLabel.setText("🎯 Click a big board to nuke."); return; }
                if (bigBoard[br][bc] != '\0') {
                    statusLabel.setText("⚠ That board is already claimed!");
                    return;
                }
                for (int r = 0; r < SIZE; r++) {
                    for (int c = 0; c < SIZE; c++) {
                        if (!anchored[br][bc][r][c]) {
                            smallBoard[br][bc][r][c] = '\0';
                            updateCellUI(br, bc, r, c);
                        }
                    }
                }
                statusLabel.setText("☢ NUKED board (" + br + "," + bc + ")!");
                SoundManager.playSFX("sfx_gunshot.mp3");
            }

            case SWAP_SYMBOLS -> {
                for (int r = 0; r < SIZE; r++)
                    for (int c = 0; c < SIZE; c++)
                        for (int sr2 = 0; sr2 < SIZE; sr2++)
                            for (int sc2 = 0; sc2 < SIZE; sc2++) {
                                char cell = smallBoard[r][c][sr2][sc2];
                                if (cell == 'X') smallBoard[r][c][sr2][sc2] = 'O';
                                else if (cell == 'O') smallBoard[r][c][sr2][sc2] = 'X';
                            }
                // Also swap big board
                for (int r = 0; r < SIZE; r++)
                    for (int c = 0; c < SIZE; c++) {
                        if (bigBoard[r][c] == 'X') bigBoard[r][c] = 'O';
                        else if (bigBoard[r][c] == 'O') bigBoard[r][c] = 'X';
                    }
                rebuildAllCellUI();
                statusLabel.setText("⇄ All symbols swapped!");
                SoundManager.playSFX("sfx_ui_accept_death.mp3");
            }

            case CLAIM -> {
                if (br == -1) { statusLabel.setText("🎯 Click an unclaimed big board."); return; }
                if (bigBoard[br][bc] != '\0') {
                    statusLabel.setText("⚠ Already claimed!");
                    return;
                }
                bigBoard[br][bc] = currentPlayer;
                showBigCellClaim(br, bc, currentPlayer);
                char bigWinner = checkWinner(bigBoard);
                if (bigWinner != '\0') { endGame(bigWinner); return; }
                statusLabel.setText("★ Claimed board (" + br + "," + bc + ")!");
                SoundManager.playSFX("sfx_powerup.mp3");
            }

            case MIRROR -> {
                if (lastCardPlayed == null || lastCardPlayed.getType() == Card.CardType.MIRROR) {
                    statusLabel.setText("⚠ Nothing to mirror!");
                    return;
                }
                statusLabel.setText("↩ Mirroring: " + lastCardPlayed.getDisplayName());
                // Re-activate last card as current player — instant only for safety
                if (lastCardPlayed.getTargetType() == Card.TargetType.INSTANT) {
                    Card mirrorCard = lastCardPlayed;
                    lastCardPlayed = card; // prevent infinite mirror loop
                    hand.remove(card);
                    cardUsedThisTurn = true;
                    selectedCard = null;
                    selectedCardButton = null;
                    updateHandUI();
                    activateCard(mirrorCard, -1, -1, -1, -1);
                    return;
                } else {
                    statusLabel.setText("↩ Mirror only works on instant cards!");
                    return;
                }
            }
        }

        // Remove card from hand
        lastCardPlayed = card;
        hand.remove(card);
        cardUsedThisTurn = true;
        selectedCard = null;
        if (selectedCardButton != null) {
            selectedCardButton = null;
        }
        updateHandUI();
        highlightActiveBoards();
    }

    // --- Board Highlighting ---

    private void highlightActiveBoards() {
        for (int br = 0; br < SIZE; br++) {
            for (int bc = 0; bc < SIZE; bc++) {
                boolean active = isBoardActive(br, bc);
                bigPanes[br][bc].setStyle(bigCellStyle(active));

                // Enable/disable buttons
                if (bigBoard[br][bc] != '\0') continue;
                for (int sr = 0; sr < SIZE; sr++) {
                    for (int sc = 0; sc < SIZE; sc++) {
                        cellButtons[br][bc][sr][sc].setDisable(!active || smallBoard[br][bc][sr][sc] != '\0');
                    }
                }
            }
        }
    }

    private boolean isBoardActive(int br, int bc) {
        if (bigBoard[br][bc] != '\0') return false;
        if (forcedBigRow == -1) return true;
        return br == forcedBigRow && bc == forcedBigCol;
    }

    // --- UI Update Helpers ---

    private void updateCellUI(int br, int bc, int sr, int sc) {
        Button btn = cellButtons[br][bc][sr][sc];
        char val = smallBoard[br][bc][sr][sc];
        boolean isAnchored = anchored[br][bc][sr][sc];

        switch (val) {
            case 'X' -> {
                btn.setText("X");
                btn.setStyle(isAnchored ? anchoredCellStyle("X") : xCellStyle());
            }
            case 'O' -> {
                btn.setText("O");
                btn.setStyle(isAnchored ? anchoredCellStyle("O") : oCellStyle());
            }
            case '#' -> {
                btn.setText("■");
                btn.setStyle(blockedCellStyle());
                btn.setDisable(true);
            }
            default -> {
                btn.setText("");
                btn.setStyle(emptyCellStyle());
            }
        }
    }

    private void rebuildAllCellUI() {
        for (int br = 0; br < SIZE; br++)
            for (int bc = 0; bc < SIZE; bc++)
                for (int sr = 0; sr < SIZE; sr++)
                    for (int sc = 0; sc < SIZE; sc++)
                        updateCellUI(br, bc, sr, sc);

        // Rebuild claimed boards
        for (int br = 0; br < SIZE; br++)
            for (int bc = 0; bc < SIZE; bc++)
                if (bigBoard[br][bc] != '\0')
                    showBigCellClaim(br, bc, bigBoard[br][bc]);
    }

    private void showBigCellClaim(int br, int bc, char winner) {
        SoundManager.playSFX("sfx_break.mp3");
        StackPane pane = bigPanes[br][bc];
        pane.getChildren().clear();

        Label claimLabel = new Label(winner == 'D' ? "—" : String.valueOf(winner));
        claimLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 72));
        claimLabel.setTextFill(winner == 'X' ? Color.web("#e94560") :
                winner == 'O' ? Color.web("#4caf50") :
                        Color.web("#555588"));
        pane.getChildren().add(claimLabel);
        pane.setStyle(bigCellStyle(false));
    }

    private void updateTurnLabel() {
        turnLabel.setText((currentPlayer == 'X' ? "Player X's" : "Player O's") + " Turn");
        turnLabel.setStyle("-fx-font-family: Monospace; -fx-font-size: 18; -fx-text-fill: "
                + (currentPlayer == 'X' ? "#e94560" : "#4caf50") + ";");
        p1Label.setStyle("-fx-font-family: Monospace; -fx-font-size: 14; -fx-text-fill: "
                + (currentPlayer == 'X' ? "#e94560" : "#aaaaaa") + ";");
        p2Label.setStyle("-fx-font-family: Monospace; -fx-font-size: 14; -fx-text-fill: "
                + (currentPlayer == 'O' ? "#4caf50" : "#aaaaaa") + ";");
    }

    private void endGame(char winner) {
        gameOver = true;
        matchWinner = winner; // Store the winner for the upload button
        restartButton.setVisible(true);

        if (winner == 'D') {
            statusLabel.setText("🤝 It's a draw!");
            statusLabel.setStyle("-fx-text-fill: #aaaaaa;");
        } else {
            statusLabel.setText("🏆 Player " + winner + " wins!");
            statusLabel.setStyle("-fx-text-fill: " + (winner == 'X' ? "#e94560" : "#4caf50") + ";");
        }
        SoundManager.playSFX("sfx_powerup.mp3");

        // Disable all cells
        for (int br = 0; br < SIZE; br++)
            for (int bc = 0; bc < SIZE; bc++)
                for (int sr = 0; sr < SIZE; sr++)
                    for (int sc = 0; sc < SIZE; sc++)
                        cellButtons[br][bc][sr][sc].setDisable(true);

        if (savePane != null) {
            savePane.setVisible(true);
            savePane.setManaged(true);
        }
    }

    // --- Game Logic Helpers ---

    private char checkWinner(char[][] board) {
        // Rows and cols
        for (int i = 0; i < SIZE; i++) {
            if (board[i][0] != '\0' && board[i][0] != 'D' && board[i][0] != '#'
                    && board[i][0] == board[i][1] && board[i][1] == board[i][2]) return board[i][0];
            if (board[0][i] != '\0' && board[0][i] != 'D' && board[0][i] != '#'
                    && board[0][i] == board[1][i] && board[1][i] == board[2][i]) return board[0][i];
        }
        // Diagonals
        if (board[0][0] != '\0' && board[0][0] != 'D' && board[0][0] != '#'
                && board[0][0] == board[1][1] && board[1][1] == board[2][2]) return board[0][0];
        if (board[0][2] != '\0' && board[0][2] != 'D' && board[0][2] != '#'
                && board[0][2] == board[1][1] && board[1][1] == board[2][0]) return board[0][2];
        return '\0';
    }

    private boolean isBoardFull(char[][] board) {
        for (char[] row : board)
            for (char c : row)
                if (c == '\0') return false;
        return true;
    }

    private boolean isAllBoardsFull() {
        for (char[] row : bigBoard)
            for (char c : row)
                if (c == '\0') return false;
        return true;
    }

    private List<int[]> findCells(char symbol, boolean includeAnchored) {
        List<int[]> result = new ArrayList<>();
        for (int br = 0; br < SIZE; br++)
            for (int bc = 0; bc < SIZE; bc++)
                if (bigBoard[br][bc] == '\0')
                    for (int sr = 0; sr < SIZE; sr++)
                        for (int sc = 0; sc < SIZE; sc++)
                            if (smallBoard[br][bc][sr][sc] == symbol)
                                if (includeAnchored || !anchored[br][bc][sr][sc])
                                    result.add(new int[]{br, bc, sr, sc});
        return result;
    }

    private List<int[]> findEmptyCells() {
        List<int[]> result = new ArrayList<>();
        for (int br = 0; br < SIZE; br++)
            for (int bc = 0; bc < SIZE; bc++)
                if (bigBoard[br][bc] == '\0')
                    for (int sr = 0; sr < SIZE; sr++)
                        for (int sc = 0; sc < SIZE; sc++)
                            if (smallBoard[br][bc][sr][sc] == '\0')
                                result.add(new int[]{br, bc, sr, sc});
        return result;
    }

    private List<Card> getCurrentHand() {
        return currentPlayer == 'X' ? p1Cards : p2Cards;
    }

    private List<Card> getHandFor(char player) {
        return player == 'X' ? p1Cards : p2Cards;
    }

    @FXML
    private void handleRestart() {
        SoundManager.playSFX("sfx_ui_confirm.mp3");
        newGame();
    }

    @FXML
    private void handleQuit() {
        SoundManager.playSFX("sfx_ui_accept_death.mp3");
        SoundManager.stopMusic();
        quitToLobby();
    }

    // --- Styles ---

    private String bigCellStyle(boolean active) {
        String border = active ? "#f5a623" : "#333355";
        return "-fx-border-color: " + border + "; -fx-border-width: 3; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-background-color: " + (active ? "#1e1e3a" : "#16213e") + ";";
    }

    private String emptyCellStyle() {
        return "-fx-background-color: #0f1030; -fx-border-color: #333366; " +
                "-fx-border-radius: 3; -fx-background-radius: 3; -fx-text-fill: white;";
    }

    private String xCellStyle() {
        return "-fx-background-color: #2a0a14; -fx-border-color: #e94560; " +
                "-fx-border-radius: 3; -fx-background-radius: 3; " +
                "-fx-text-fill: #e94560; -fx-font-weight: bold; -fx-font-size: 16;";
    }

    private String oCellStyle() {
        return "-fx-background-color: #0a2a14; -fx-border-color: #4caf50; " +
                "-fx-border-radius: 3; -fx-background-radius: 3; " +
                "-fx-text-fill: #4caf50; -fx-font-weight: bold; -fx-font-size: 16;";
    }

    private String anchoredCellStyle(String symbol) {
        String base = symbol.equals("X") ? xCellStyle() : oCellStyle();
        return base + " -fx-border-width: 2;";
    }

    private String blockedCellStyle() {
        return "-fx-background-color: #1a1a2e; -fx-border-color: #555555; " +
                "-fx-border-radius: 3; -fx-background-radius: 3; " +
                "-fx-text-fill: #555555; -fx-font-size: 14;";
    }

    private String cardStyle(Card card, boolean selected) {
        String bg = selected ? "#f5a623" :
                card.getRarity() == Card.Rarity.RARE ? "#3a0a2a" : "#16213e";
        String border = card.getRarity() == Card.Rarity.RARE ? "#e94560" : "#444466";
        String textColor = selected ? "#000000" :
                card.getRarity() == Card.Rarity.RARE ? "#ff6699" : "#eaeaea";
        return "-fx-background-color: " + bg + "; -fx-border-color: " + border + "; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-text-fill: " + textColor + "; -fx-font-family: Monospace; " +
                "-fx-font-size: 11; -fx-cursor: hand; -fx-border-width: 2;";
    }

    @FXML
    private void handleSaveMatch() {
        String p1Alias = p1NameInput.getText().trim();
        String p2Alias = p2NameInput.getText().trim();

        if (p1Alias.isEmpty() || p2Alias.isEmpty()) {
            statusLabel.setText("⚠ Both players must enter an alias to upload!");
            statusLabel.setStyle("-fx-text-fill: #f5a623;");
            return;
        }

        UTTTDAO.saveMatch(p1Alias, p2Alias, matchWinner);

        saveScoreBtn.setText("Match Uploaded!");
        saveScoreBtn.setDisable(true);
        p1NameInput.setDisable(true);
        p2NameInput.setDisable(true);
    }
}