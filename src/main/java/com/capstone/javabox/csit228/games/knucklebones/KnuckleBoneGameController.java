package com.capstone.javabox.csit228.games.knucklebones;


import com.capstone.javabox.csit228.games.JavaboxAbstractController;
import com.capstone.javabox.csit228.database.KnucklebonesDAO;
import com.capstone.javabox.csit228.utils.JavaboxUtils;
import com.capstone.javabox.csit228.utils.SoundManager;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/*Hey there! Are you debugging or seeing how Knucklebones work?
 * KBPlayer handles the math [and the board]
 * This class simply updates the view based on the boards and scores of KBPlayer
 * */

public class KnuckleBoneGameController extends JavaboxAbstractController {

    @FXML private StackPane rootPane; // THE JUICE: For Screen Shake

    @FXML private Label turn;
    @FXML private Label p1_die;
    @FXML private Label p2_die;
    @FXML private Label p1_name;
    @FXML private Label p2_name;
    @FXML private Label p1_score;
    @FXML private Label p2_score;
    @FXML private ProgressBar bar;

    //Grid Labels [col, row]
    @FXML private Label p1_00;  //PLAYER 1
    @FXML private Label p1_01;
    @FXML private Label p1_02;

    @FXML private Label p1_10;
    @FXML private Label p1_11;
    @FXML private Label p1_12;

    @FXML private Label p1_20;
    @FXML private Label p1_21;
    @FXML private Label p1_22;

    @FXML private Label p2_00;  //PLAYER 2
    @FXML private Label p2_01;
    @FXML private Label p2_02;

    @FXML private Label p2_10;
    @FXML private Label p2_11;
    @FXML private Label p2_12;

    @FXML private Label p2_20;
    @FXML private Label p2_21;
    @FXML private Label p2_22;

    //Grid Buttons
    @FXML private Button p1_c0;
    @FXML private Button p1_c1;
    @FXML private Button p1_c2;

    @FXML private Button p2_c0;
    @FXML private Button p2_c1;
    @FXML private Button p2_c2;

    //Effects and Juice
    private DropShadow p1Glow;
    private DropShadow p2Glow;
    @FXML private GridPane p1_grid; // Player 1's board
    @FXML private GridPane p2_grid; // Player 2's board
    @FXML private Pane particleLayer;
    private Random random = new Random();
    private Timeline pulseAnimation;

    // Global Hex Colors for Juice Effects
    private String hex1;
    private String hex2;

    private KBPlayer p1;
    private KBPlayer p2;
    private KBPlayer current;
    private int dice_val;
    private boolean isAnimating = false; //Prevent clicks during dice roll

    @FXML
    public void initialize() {
        p1_die.setText("");
        p2_die.setText("");
        p1_score.setText("0");
        p2_score.setText("0");
    }

    public void setPlayerData(String name1, String color1, String name2, String color2) {
        p1 = new KBPlayer(name1);
        p2 = new KBPlayer(name2);
        p1_name.setText(name1);
        p2_name.setText(name2);

        // Save to class variables so spawnSparks can use them later
        hex1 = JavaboxUtils.extractHex(color1);
        hex2 = JavaboxUtils.extractHex(color2);

        p1_name.setStyle("-fx-text-fill: " + hex1 + ";");
        p2_name.setStyle("-fx-text-fill: " + hex2 + ";");
        p1_score.setStyle("-fx-text-fill: " + hex1 + ";");
        p2_score.setStyle("-fx-text-fill: " + hex2 + ";");
        p1_die.setStyle("-fx-text-fill: " + hex1 + ";");
        p2_die.setStyle("-fx-text-fill: " + hex2 + ";");

        //Set player 1's grid labels with p1's colors
        for (javafx.scene.Node node : p1_grid.getChildren()) {
            if (node instanceof javafx.scene.control.Label) {
                node.setStyle("-fx-text-fill: " + hex1 + ";");
            }
        }

        //Set player 2's grid labels with p2's colors
        for (javafx.scene.Node node : p2_grid.getChildren()) {
            if (node instanceof javafx.scene.control.Label) {
                node.setStyle("-fx-text-fill: " + hex2 + ";");
            }
        }

        //Glow
        p1Glow = new DropShadow(javafx.scene.effect.BlurType.GAUSSIAN, Color.web(hex1), 15, 0.4, 0, 0);
        p2Glow = new DropShadow(javafx.scene.effect.BlurType.GAUSSIAN, Color.web(hex2), 15, 0.4, 0, 0);

        pulseAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(p1Glow.spreadProperty(), 0.4),
                        new KeyValue(p1Glow.radiusProperty(), 15),
                        new KeyValue(p2Glow.spreadProperty(), 0.4),
                        new KeyValue(p2Glow.radiusProperty(), 15)
                ),
                new KeyFrame(Duration.millis(800),
                        // Crank the spread to 0.8 so it becomes a thick, sharp neon line at its peak
                        // (Keep your custom SPLINE interpolator here if you prefer it!)
                        new KeyValue(p1Glow.spreadProperty(), 0.8, Interpolator.SPLINE(0.5, 0.1, 0.5, 0.9)),
                        new KeyValue(p1Glow.radiusProperty(), 20, Interpolator.SPLINE(0.5, 0.1, 0.5, 0.9)),
                        new KeyValue(p2Glow.spreadProperty(), 0.8, Interpolator.SPLINE(0.5, 0.1, 0.5, 0.9)),
                        new KeyValue(p2Glow.radiusProperty(), 20, Interpolator.SPLINE(0.5, 0.1, 0.5, 0.9))
                )
        );
        pulseAnimation.setAutoReverse(true);
        pulseAnimation.setCycleCount(Timeline.INDEFINITE);
        pulseAnimation.play();

        current = p1;
        updateTurnUI();
        setupProgressBar(color1, color2);
        rollDiceWithAnimation();
    }

    public void rollDiceWithAnimation() {
        SoundManager.playSFX("sfx_dice.mp3");
        isAnimating = true;
        Label activeDieLabel = (current.equals(p1)) ? p1_die : p2_die;
        Label inactiveDieLabel = (current.equals(p1)) ? p2_die : p1_die;

        inactiveDieLabel.setText("");
        activeDieLabel.setScaleX(1.0);
        activeDieLabel.setScaleY(1.0);

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            activeDieLabel.setText(String.valueOf(ThreadLocalRandom.current().nextInt(1, 7)));
        }));
        timeline.setCycleCount(6);

        timeline.setOnFinished(e -> {
            dice_val = ThreadLocalRandom.current().nextInt(1, 7);
            activeDieLabel.setText(String.valueOf(dice_val));
            activeDieLabel.setScaleX(1.4);
            activeDieLabel.setScaleY(1.4);
            isAnimating = false;
        });

        timeline.play();
    }

    @FXML
    public void onColumnClicked(ActionEvent event) {
        if (isAnimating) return; //Ignore clicks if dice is still rolling

        Button clickedBtn = (Button) event.getSource();
        String id = clickedBtn.getId(); // e.g., "p1_c0"

        String expectedPrefix = (current.equals(p1)) ? "p1" : "p2";
        if (!id.startsWith(expectedPrefix)) return;

        int col = Character.getNumericValue(id.charAt(id.length() - 1));

        int targetRow = -1;
        for (int r = 2; r >= 0; r--) {
            if (current.board[r][col] == 0) {
                targetRow = r;
                break;
            }
        }

        if (insertBoard(col)) {
            refreshBoardUI();

            String labelId = "#" + expectedPrefix + "_" + col + targetRow;
            Label exactCellLabel = (Label) turn.getScene().lookup(labelId);

            GridPane activeGrid = current.equals(p1) ? p1_grid : p2_grid;
            activeGrid.layout();

            spawnSparks(exactCellLabel != null ? exactCellLabel : clickedBtn, current.equals(p1) ? hex1 : hex2);

            if (isBoardFull(current)) {
                handleGameOver();
            } else {
                setCurrent();
                updateTurnUI();
                rollDiceWithAnimation();
            }
        } else {
            JavaboxUtils.showAlert(Alert.AlertType.INFORMATION, "Column Full", null, "Pick another spot!");
        }
    }

    private boolean isBoardFull(KBPlayer player) {
        for (int col = 0; col < 3; col++) {
            if (player.board[0][col] == 0) {
                return false;
            }
        }
        return true;
    }

    public boolean insertBoard(int col) {
        boolean inserted = current.insertBoard(col, dice_val);
        if (inserted) {
            KBPlayer opponent = (current.equals(p1)) ? p2 : p1;
            boolean attackLanded = opponent.receiveAttack(col, dice_val);
            if (attackLanded) {
                SoundManager.playSFX("sfx_explosion.mp3");
                System.out.println("YO!!! " + current.name + " destroyed a die!");
                shakeScreen();
            }
        }
        return inserted;
    }

    private void handleGameOver() {
        String result;
        int s1 = p1.getScore();
        int s2 = p2.getScore();

        if (s1 > s2) result = p1.name + " wins!";
        else if (s2 > s1) result = p2.name + " wins!";
        else result = "It's a tie!";

        KnucklebonesDAO.saveMatch(p1.name, s1, p2.name, s2);

        JavaboxUtils.showAlert(
                Alert.AlertType.INFORMATION,
                "GAME OVER", result,
                p1.name + ": " + s1 + " | " + p2.name + ": " + s2
        );

        //Return to menu
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("knucklebone-menu.fxml"));
            Scene gameScene = new Scene(loader.load(), 1280, 720);
            KnuckleBoneMenuController menuController = getKnuckleBoneMenuController(loader);
            menuController.setQuitCallback(this.quitCallback);

            Stage stage = (Stage) p1_score.getScene().getWindow();

            stage.setTitle("Knucklebones - Menu");
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

    private KnuckleBoneMenuController getKnuckleBoneMenuController(FXMLLoader loader) {
        return loader.getController();
    }

    public void setCurrent() {
        current = (current.equals(p1)) ? p2 : p1;
    }

    public void updateTurnUI() {
        turn.setText(current.name.toUpperCase() + "'S TURN");

        if (current.equals(p1)) {
            p1_grid.setEffect(p1Glow);
            p2_grid.setEffect(null);
        } else {
            p2_grid.setEffect(p2Glow);
            p1_grid.setEffect(null);
        }
    }

    private void refreshBoardUI() {
        SoundManager.playSFX("sfx_junimo.mp3");
        updateGrid(p1, "p1");
        updateGrid(p2, "p2");
        p1_score.setText(String.valueOf(p1.getScore()));
        p2_score.setText(String.valueOf(p2.getScore()));
        updateProgressBarSmoothly();
    }

    private void updateGrid(KBPlayer player, String prefix) {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                Label l = (Label) turn.getScene().lookup("#" + prefix + "_" + c + r);

                if (l != null) {
                    int val = player.board[r][c];
                    l.setText(val == 0 ? "" : String.valueOf(val));
                }
            }
        }
    }

    public void setupProgressBar(String color1, String color2) {
        //Set the colors from the lobby selection
        bar.setStyle(
                "-fx-accent: " + JavaboxUtils.extractHex(color1) + "; " +
                        "-fx-control-inner-background: " + JavaboxUtils.extractHex(color2) + ";"
        );
        bar.setProgress(0.5);
    }

    private void updateProgressBarSmoothly() {
        double p1Total = p1.getScore();
        double p2Total = p2.getScore();
        double combined = p1Total + p2Total;
        double targetProgress = (combined == 0) ? 0.5 : (p1Total / combined);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(600),
                        new KeyValue(bar.progressProperty(), targetProgress, Interpolator.EASE_BOTH))
        );
        timeline.play();
    }

    //Effects and Juice (glugh gluhg)
    private void shakeScreen() {
        if (rootPane == null) {
            System.err.println("Shake failed: rootPane is null. Ensure StackPane has fx:id=\"rootPane\" in FXML.");
            return;
        }

        rootPane.setTranslateX(0);
        rootPane.setTranslateY(0);

        Timeline shakeTimeline = new Timeline();
        int frames = 8;        //How many shakes
        int durationPerFrame = 35; //Milliseconds per shake

        for (int i = 0; i < frames; i++) {
            double offsetX = (random.nextDouble() * 12) - 6;
            double offsetY = (random.nextDouble() * 12) - 6;

            KeyFrame keyFrame = new KeyFrame(
                    Duration.millis(i * durationPerFrame),
                    new KeyValue(rootPane.translateXProperty(), offsetX, Interpolator.DISCRETE),
                    new KeyValue(rootPane.translateYProperty(), offsetY, Interpolator.DISCRETE)
            );
            shakeTimeline.getKeyFrames().add(keyFrame);
        }

        //Back to Zero
        KeyFrame snapBack = new KeyFrame(
                Duration.millis(frames * durationPerFrame),
                new KeyValue(rootPane.translateXProperty(), 0, Interpolator.LINEAR),
                new KeyValue(rootPane.translateYProperty(), 0, Interpolator.LINEAR)
        );
        shakeTimeline.getKeyFrames().add(snapBack);

        shakeTimeline.play();
    }

    private void spawnSparks(javafx.scene.Node targetNode, String hexColor) {
        if (particleLayer == null) return;

        javafx.geometry.Point2D sceneCoords = targetNode.localToScene(targetNode.getBoundsInLocal().getWidth() / 2, targetNode.getBoundsInLocal().getHeight() / 2);
        javafx.geometry.Point2D layerCoords = particleLayer.sceneToLocal(sceneCoords);

        double startX = layerCoords.getX();
        double startY = layerCoords.getY();

        Color playerColor = Color.web(hexColor);
        int particleCount = 15;

        for (int i = 0; i < particleCount; i++) {
            Circle spark = new Circle(random.nextDouble() * 3 + 1, playerColor); // Radius between 1 and 4
            spark.setLayoutX(startX);
            spark.setLayoutY(startY);

            DropShadow sparkGlow = new DropShadow(javafx.scene.effect.BlurType.GAUSSIAN, playerColor, 5, 0.6, 0, 0);
            spark.setEffect(sparkGlow);

            particleLayer.getChildren().add(spark);

            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = random.nextDouble() * 60 + 20;

            double targetX = startX + Math.cos(angle) * distance;
            double targetY = startY + Math.sin(angle) * distance;

            TranslateTransition move = new TranslateTransition(Duration.millis(400 + random.nextInt(200)), spark);
            move.setToX(targetX - startX);
            move.setToY(targetY - startY);

            move.setInterpolator(Interpolator.SPLINE(0.25, 0.1, 0.25, 1));

            FadeTransition fade = new FadeTransition(Duration.millis(300), spark);
            fade.setDelay(Duration.millis(200)); // Wait a tiny bit before fading
            fade.setToValue(0);

            ScaleTransition shrink = new ScaleTransition(Duration.millis(400), spark);
            shrink.setToX(0);
            shrink.setToY(0);

            ParallelTransition burst = new ParallelTransition(spark, move, fade, shrink);
            burst.setOnFinished(e -> particleLayer.getChildren().remove(spark));

            burst.play();
        }
    }
}