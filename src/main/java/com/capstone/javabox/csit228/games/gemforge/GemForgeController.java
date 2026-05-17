package com.capstone.javabox.csit228.games.gemforge;

import com.capstone.javabox.csit228.games.JavaboxAbstractController;
import com.capstone.javabox.csit228.utils.SoundManager;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class GemForgeController extends JavaboxAbstractController {

    @FXML private Pane particleCanvas; // The new background layer
    @FXML private VBox setupPane;
    @FXML private ComboBox<Integer> comboPlayerCount;
    @FXML private BorderPane gamePane;

    @FXML private VBox winPane;
    @FXML private Label lblWinner;

    @FXML private GridPane boardGrid;
    @FXML private Label lblTurn;

    private static final int SIZE = 6;
    private int[][] tileHP = new int[SIZE][SIZE];
    private StackPane[][] uiCells = new StackPane[SIZE][SIZE];

    private List<Gem> players = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private boolean gameOver = false;
    private boolean isAnimating = false;

    private AnimationTimer particleLoop;

    private enum Direction { UP, DOWN, LEFT, RIGHT, NONE }

    private final Interpolator SINE_EASE = new Interpolator() {
        @Override
        protected double curve(double t) {
            return 0.5 * (1.0 - Math.cos(Math.PI * t));
        }
    };

    private class Gem {
        String name; Color color;
        int row, col;
        boolean isAlive = true;
        Direction lastDir = Direction.NONE;

        Gem(String name, Color color, int r, int c) {
            this.name = name; this.color = color;
            this.row = r; this.col = c;
        }
    }

    @FXML
    public void initialize() {
        SoundManager.playMusic(true, "music_gem_forge1.mp3", "music_gem_forge2.mp3");
        comboPlayerCount.getItems().addAll(2, 3, 4);
        comboPlayerCount.setValue(2);

        setupPane.setVisible(true);
        gamePane.setVisible(false);
        winPane.setVisible(false);

        startEmberParticleEngine();
    }

    private void startEmberParticleEngine() {
        // Dynamically clip the canvas bounds
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(particleCanvas.widthProperty());
        clip.heightProperty().bind(particleCanvas.heightProperty());
        particleCanvas.setClip(clip);

        particleLoop = new AnimationTimer() {
            private long lastSpawnTime = 0;
            @Override
            public void handle(long now) {
                if (now - lastSpawnTime > 80_000_000) {
                    spawnEmber();
                    lastSpawnTime = now;
                }
            }
        };
        particleLoop.start();
    }

    private void spawnEmber() {
        double canvasWidth = particleCanvas.getWidth();
        double canvasHeight = particleCanvas.getHeight();

        if (canvasWidth == 0 || canvasHeight == 0) return;

        Color baseColor;

        // Determine ambient color based on the current game state!
        if (setupPane.isVisible()) {
            baseColor = Color.web("#ff6600"); // Fiery orange for the setup menu
        } else if (gameOver) {
            // Find the winner's color to celebrate
            Gem winner = null;
            for (Gem g : players) {
                if (g.isAlive) winner = g;
            }
            baseColor = (winner != null) ? winner.color : Color.WHITE;
        } else {
            // Match the exact color of the player whose turn it is!
            baseColor = players.get(currentPlayerIndex).color;
        }

        // Apply a random opacity to give it that flickering ember glow
        double opacity = 0.3 + (Math.random() * 0.5);
        Color emberColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), opacity);

        double radius = 1.5 + (Math.random() * 3.5);
        Circle ember = new Circle(radius, emberColor);

        // Spawn randomly across the X axis
        double startX = Math.random() * canvasWidth;
        // Spawn strictly below the bottom edge
        double startY = canvasHeight + 15;

        ember.setCenterX(startX);
        ember.setCenterY(startY);

        particleCanvas.getChildren().add(ember);

        double travelDuration = 1800 + (Math.random() * 1400);
        TranslateTransition rise = new TranslateTransition(Duration.millis(travelDuration), ember);

        double driftX = -20 + (Math.random() * 40);
        rise.setByX(driftX);
        rise.setByY(-(canvasHeight + 60));

        FadeTransition coolDown = new FadeTransition(Duration.millis(travelDuration), ember);
        coolDown.setToValue(0.0);

        ParallelTransition lifeCycle = new ParallelTransition(rise, coolDown);
        lifeCycle.setOnFinished(e -> particleCanvas.getChildren().remove(ember));
        lifeCycle.play();
    }

    @FXML
    private void handleStartGame() {
        SoundManager.playSFX("sfx_powerup.mp3");
        int playerCount = comboPlayerCount.getValue();

        generateBoardPattern();

        boardGrid.getChildren().clear();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(80, 80);
                int finalR = r; int finalC = c;
                cell.setOnMouseClicked(e -> handleCellClick(finalR, finalC));

                uiCells[r][c] = cell;
                boardGrid.add(cell, c, r);
            }
        }

        players.clear();
        players.add(new Gem("RUBY", Color.web("#ff4d4d"), 1, 1));
        players.add(new Gem("SAPPHIRE", Color.web("#3399ff"), 4, 4));
        if (playerCount >= 3) players.add(new Gem("EMERALD", Color.web("#33cc33"), 1, 4));
        if (playerCount == 4) players.add(new Gem("TOPAZ", Color.web("#ffcc00"), 4, 1));

        currentPlayerIndex = 0;
        gameOver = false;
        isAnimating = false;

        updateTurnLabel();
        drawBoard();

        setupPane.setVisible(false);
        gamePane.setVisible(true);
        winPane.setVisible(false);
    }

    private void generateBoardPattern() {
        int patternType = (int) (Math.random() * 6);

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                tileHP[r][c] = 2; // Baseline floor

                if (patternType == 0) {
                    if ((r == 2 || r == 3) && (c == 2 || c == 3)) tileHP[r][c] = 1;
                    else if ((r == 0 || r == SIZE - 1) && (c == 0 || c == SIZE - 1)) tileHP[r][c] = 3;

                } else if (patternType == 1) {
                    if ((r == 0 || r == SIZE - 1) && (c == 2 || c == 3)) tileHP[r][c] = 1;
                    else if ((r == 2 || r == 3) && (c == 2 || c == 3)) tileHP[r][c] = 3;

                } else if (patternType == 2) {
                    if ((r == 0 || r == SIZE - 1) && (c == 0 || c == SIZE - 1)) tileHP[r][c] = 1;
                    else if ((r == 1 || r == 4) && (c == 2 || c == 3) ||
                            (c == 1 || c == 4) && (r == 2 || r == 3)) tileHP[r][c] = 3;

                } else if (patternType == 3) {
                    if (r == 2 || r == 3 || c == 2 || c == 3) {
                        tileHP[r][c] = 3;
                    } else {
                        tileHP[r][c] = 1;
                    }

                } else if (patternType == 4) {
                    if ((r + c) % 2 == 0) {
                        tileHP[r][c] = 1;
                    } else {
                        tileHP[r][c] = 2;
                    }
                    if ((r == 0 || r == SIZE - 1) && (c == 2 || c == 3)) tileHP[r][c] = 3;

                } else {
                    if (r >= 1 && r <= 4 && c >= 1 && c <= 4) {
                        if (r == 1 || r == 4 || c == 1 || c == 4) {
                            tileHP[r][c] = 1;
                        } else {
                            tileHP[r][c] = 3;
                        }
                    }
                }

                if ((r == 1 && c == 1) || (r == 1 && c == SIZE - 2) ||
                        (r == SIZE - 2 && c == 1) || (r == SIZE - 2 && c == SIZE - 2)) {
                    tileHP[r][c] = 3;
                }
            }
        }
    }

    @FXML
    private void handleReturnToMenu() {
        SoundManager.playSFX("sfx_powerup.mp3");
        setupPane.setVisible(true);
        gamePane.setVisible(false);
        winPane.setVisible(false);
    }

    @FXML
    private void handleExit() {
        SoundManager.playSFX("sfx_ui_accept_death.mp3");
        SoundManager.playMusic(true, "music_lobby_music1.mp3", "music_lobby_music2.mp3");
        quitToLobby();
    }

    private void handleCellClick(int targetR, int targetC) {
        if (gameOver || isAnimating) return;

        Gem current = players.get(currentPlayerIndex);
        if (!current.isAlive) return;

        if (!isValidMove(current, targetR, targetC)) return;

        int startR = current.row;
        int startC = current.col;
        int dx = Integer.compare(targetC, startC);
        int dy = Integer.compare(targetR, startR);
        Direction attemptDir = getDirection(dx, dy);

        Circle currentVis = getGemVisual(startR, startC);
        if (currentVis != null) uiCells[startR][startC].setViewOrder(-3);

        List<Gem> pushedChain = new ArrayList<>();
        List<Circle> chainedVis = new ArrayList<>();
        List<int[]> chainedOldPos = new ArrayList<>();

        int currCheckR = targetR;
        int currCheckC = targetC;
        Gem chainGem = getGemAt(currCheckR, currCheckC);

        if (dx != 0 || dy != 0) {
            while (chainGem != null) {
                pushedChain.add(chainGem);
                Circle vis = getGemVisual(chainGem.row, chainGem.col);
                if (vis != null) {
                    uiCells[chainGem.row][chainGem.col].setViewOrder(-2);
                    chainedVis.add(vis);
                    chainedOldPos.add(new int[]{chainGem.row, chainGem.col});
                }

                currCheckR += dy;
                currCheckC += dx;
                chainGem = getGemAt(currCheckR, currCheckC);
            }
        }

        boolean hitsWall = (currCheckR < 0 || currCheckR >= SIZE || currCheckC < 0 || currCheckC >= SIZE);

        Gem secondaryTarget = null;
        Circle secondaryVis = null;
        int oldSecR = -1, oldSecC = -1;
        int recoilR = -1, recoilC = -1;

        if (startR == targetR && startC == targetC) {
            degradeTile(startR, startC);
            current.lastDir = Direction.NONE;
            checkLavaDeath(current);
        } else if (pushedChain.isEmpty()) {
            degradeTile(startR, startC);
            current.row = targetR;
            current.col = targetC;
            current.lastDir = attemptDir;
            checkLavaDeath(current);
        } else {
            degradeTile(startR, startC);

            if (hitsWall) {
                recoilR = targetR - dy;
                recoilC = targetC - dx;

                secondaryTarget = getGemAt(recoilR, recoilC);
                if (secondaryTarget != null && secondaryTarget != current) {
                    secondaryVis = getGemVisual(secondaryTarget.row, secondaryTarget.col);
                    if (secondaryVis != null) uiCells[secondaryTarget.row][secondaryTarget.col].setViewOrder(-1);
                    oldSecR = secondaryTarget.row;
                    oldSecC = secondaryTarget.col;

                    secondaryTarget.row -= dy;
                    secondaryTarget.col -= dx;
                    secondaryTarget.lastDir = getOppositeDirection(attemptDir);
                    checkLavaDeath(secondaryTarget);
                }

                current.row = recoilR;
                current.col = recoilC;
                current.lastDir = getOppositeDirection(attemptDir);
                checkLavaDeath(current);
            } else {
                for (Gem g : pushedChain) {
                    g.row += dy;
                    g.col += dx;
                    g.lastDir = attemptDir;
                    checkLavaDeath(g);
                }

                current.row = targetR;
                current.col = targetC;
                current.lastDir = attemptDir;
                checkLavaDeath(current);
            }
        }
        isAnimating = true;
        clearArrows();
        SequentialTransition sequence = new SequentialTransition();

        if (startR == targetR && startC == targetC) {
            if (currentVis != null) {
                TranslateTransition hop = new TranslateTransition(Duration.millis(150), currentVis);
                hop.setByY(-20);
                hop.setAutoReverse(true);
                hop.setCycleCount(2);
                hop.setInterpolator(SINE_EASE);
                sequence.getChildren().add(hop);
            }
        } else if (pushedChain.isEmpty()) {
            if (currentVis != null) {
                TranslateTransition jump = new TranslateTransition(Duration.millis(300), currentVis);
                jump.setToX((targetC - startC) * 85);
                jump.setToY((targetR - startR) * 85);
                jump.setInterpolator(SINE_EASE);
                sequence.getChildren().add(jump);
            }
        } else {
            if (currentVis != null) {
                TranslateTransition strike = new TranslateTransition(Duration.millis(250), currentVis);
                strike.setToX((targetC - startC) * 85);
                strike.setToY((targetR - startR) * 85);
                strike.setInterpolator(Interpolator.EASE_IN);
                strike.setOnFinished(e -> {
                    SoundManager.playSFX("sfx_gem_hit.mp3");
                });
                sequence.getChildren().add(strike);
            }

            if (hitsWall) {
                ParallelTransition recoilPhysics = new ParallelTransition();

                if (currentVis != null) {
                    TranslateTransition bounceBack = new TranslateTransition(Duration.millis(200), currentVis);
                    bounceBack.setToX((recoilC - startC) * 85);
                    bounceBack.setToY((recoilR - startR) * 85);
                    bounceBack.setInterpolator(SINE_EASE);
                    recoilPhysics.getChildren().add(bounceBack);
                }

                for (Circle vis : chainedVis) {
                    TranslateTransition nudge = new TranslateTransition(Duration.millis(100), vis);
                    nudge.setToX(dx * 15);
                    nudge.setToY(dy * 15);
                    nudge.setAutoReverse(true);
                    nudge.setCycleCount(2);
                    nudge.setInterpolator(SINE_EASE);
                    recoilPhysics.getChildren().add(nudge);
                }

                sequence.getChildren().add(recoilPhysics);

                if (secondaryTarget != null && secondaryVis != null) {
                    TranslateTransition comboBump = new TranslateTransition(Duration.millis(250), secondaryVis);
                    comboBump.setToX((secondaryTarget.col - oldSecC) * 85);
                    comboBump.setToY((secondaryTarget.row - oldSecR) * 85);
                    comboBump.setInterpolator(SINE_EASE);
                    sequence.getChildren().add(comboBump);
                }
            } else {
                ParallelTransition pushAll = new ParallelTransition();

                for (int i = 0; i < chainedVis.size(); i++) {
                    Circle vis = chainedVis.get(i);
                    Gem g = pushedChain.get(i);
                    int[] oldPos = chainedOldPos.get(i);

                    TranslateTransition push = new TranslateTransition(Duration.millis(250), vis);
                    push.setToX((g.col - oldPos[1]) * 85);
                    push.setToY((g.row - oldPos[0]) * 85);
                    push.setInterpolator(SINE_EASE);
                    pushAll.getChildren().add(push);
                }

                sequence.getChildren().add(pushAll);
            }
        }

        sequence.setOnFinished(e -> {
            isAnimating = false;
            endTurn();
        });

        sequence.play();
    }

    private boolean isValidMove(Gem current, int targetR, int targetC) {
        int startR = current.row;
        int startC = current.col;
        if (startR != targetR && startC != targetC) return false;
        if (startR == targetR && startC == targetC) return true;

        int dx = Integer.compare(targetC, startC);
        int dy = Integer.compare(targetR, startR);
        Direction attemptDir = getDirection(dx, dy);

        if (isOpposite(current.lastDir, attemptDir)) return false;
        if (tileHP[targetR][targetC] == 0 && getGemAt(targetR, targetC) == null) return false;

        return true;
    }

    private void degradeTile(int r, int c) {
        if (tileHP[r][c] > 0){ tileHP[r][c]--; SoundManager.playSFX("sfx_glass_break.mp3");}
    }

    private void checkLavaDeath(Gem g) {
        if (!g.isAlive) return;
        if (g.row < 0 || g.row >= SIZE || g.col < 0 || g.col >= SIZE) return;
        if (tileHP[g.row][g.col] == 0){ g.isAlive = false; SoundManager.playSFX("sfx_lava_burn.mp3");}
    }

    private Gem getGemAt(int r, int c) {
        for (Gem g : players) {
            if (g.isAlive && g.row == r && g.col == c) return g;
        }
        return null;
    }

    private void endTurn() {
        drawBoard();
        checkWinCondition();
        if (gameOver) return;

        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (!players.get(currentPlayerIndex).isAlive);

        updateTurnLabel();
        drawValidArrows();
    }

    private void updateTurnLabel() {
        Gem next = players.get(currentPlayerIndex);
        lblTurn.setText("TURN: " + next.name);
        lblTurn.setStyle("-fx-text-fill: " + toHexString(next.color) + "; -fx-font-size: 24px; -fx-font-family: 'Monospace'; -fx-font-weight: bold;");
    }

    private void drawBoard() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                uiCells[r][c].getChildren().clear();

                uiCells[r][c].setViewOrder(0);

                Rectangle bg = new Rectangle(75, 75);
                bg.setArcWidth(10); bg.setArcHeight(10);

                if (tileHP[r][c] == 3) bg.setFill(Color.web("#a8b8c8"));
                else if (tileHP[r][c] == 2) bg.setFill(Color.web("#e6e6e6"));
                else if (tileHP[r][c] == 1) bg.setFill(Color.web("#d4a373"));
                else bg.setFill(Color.web("#ff3300"));

                uiCells[r][c].getChildren().add(bg);

                Gem g = getGemAt(r, c);
                if (g != null) {
                    Circle gemShape = new Circle(25, g.color);
                    gemShape.setStroke(Color.WHITE);
                    gemShape.setStrokeWidth(2);
                    uiCells[r][c].getChildren().add(gemShape);
                }
            }
        }
        if (!gameOver) drawValidArrows();
    }

    private void drawValidArrows() {
        Gem active = players.get(currentPlayerIndex);
        boolean up = false, down = false, left = false, right = false;

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (isValidMove(active, r, c)) {
                    if (r < active.row) up = true;
                    if (r > active.row) down = true;
                    if (c < active.col) left = true;
                    if (c > active.col) right = true;
                }
            }
        }

        StackPane activeCell = uiCells[active.row][active.col];

        if (up) {
            Polygon p = new Polygon(0.0, 10.0, 8.0, 0.0, 16.0, 10.0);
            p.setFill(active.color); p.setStroke(Color.WHITE); p.setStrokeWidth(1);
            StackPane.setAlignment(p, Pos.TOP_CENTER);
            StackPane.setMargin(p, new Insets(3, 0, 0, 0));
            activeCell.getChildren().add(p);
        }
        if (down) {
            Polygon p = new Polygon(0.0, 0.0, 8.0, 10.0, 16.0, 0.0);
            p.setFill(active.color); p.setStroke(Color.WHITE); p.setStrokeWidth(1);
            StackPane.setAlignment(p, Pos.BOTTOM_CENTER);
            StackPane.setMargin(p, new Insets(0, 0, 3, 0));
            activeCell.getChildren().add(p);
        }
        if (left) {
            Polygon p = new Polygon(10.0, 0.0, 0.0, 8.0, 10.0, 16.0);
            p.setFill(active.color); p.setStroke(Color.WHITE); p.setStrokeWidth(1);
            StackPane.setAlignment(p, Pos.CENTER_LEFT);
            StackPane.setMargin(p, new Insets(0, 0, 0, 3));
            activeCell.getChildren().add(p);
        }
        if (right) {
            Polygon p = new Polygon(0.0, 0.0, 10.0, 8.0, 0.0, 16.0);
            p.setFill(active.color); p.setStroke(Color.WHITE); p.setStrokeWidth(1);
            StackPane.setAlignment(p, Pos.CENTER_RIGHT);
            StackPane.setMargin(p, new Insets(0, 3, 0, 0));
            activeCell.getChildren().add(p);
        }
    }

    private void clearArrows() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                uiCells[r][c].getChildren().removeIf(node -> node instanceof Polygon);
            }
        }
    }

    private Circle getGemVisual(int r, int c) {
        if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) return null;
        for (javafx.scene.Node node : uiCells[r][c].getChildren()) {
            if (node instanceof Circle) return (Circle) node;
        }
        return null;
    }

    private void checkWinCondition() {
        int aliveCount = 0;
        Gem winner = null;
        for (Gem g : players) {
            if (g.isAlive) {
                aliveCount++;
                winner = g;
            }
        }

        if (aliveCount <= 1) {
            gameOver = true;
            winPane.setVisible(true);

            if (winner != null) {
                lblWinner.setText(winner.name + " WINS!");
                lblWinner.setStyle("-fx-text-fill: " + toHexString(winner.color) + "; -fx-font-size: 72px; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, #000000, 15, 0.8, 0, 0);");
            } else {
                lblWinner.setText("DRAW!");
                lblWinner.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 72px; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, #000000, 15, 0.8, 0, 0);");
            }
        }
    }

    private Direction getDirection(int dx, int dy) {
        if (dx == 1) return Direction.RIGHT;
        if (dx == -1) return Direction.LEFT;
        if (dy == 1) return Direction.DOWN;
        if (dy == -1) return Direction.UP;
        return Direction.NONE;
    }

    private Direction getOppositeDirection(Direction dir) {
        if (dir == Direction.UP) return Direction.DOWN;
        if (dir == Direction.DOWN) return Direction.UP;
        if (dir == Direction.LEFT) return Direction.RIGHT;
        if (dir == Direction.RIGHT) return Direction.LEFT;
        return Direction.NONE;
    }

    private boolean isOpposite(Direction last, Direction attempt) {
        return getOppositeDirection(last) == attempt && attempt != Direction.NONE;
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}