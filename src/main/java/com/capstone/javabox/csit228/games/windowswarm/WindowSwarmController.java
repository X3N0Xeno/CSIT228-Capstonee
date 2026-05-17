package com.capstone.javabox.csit228.games.windowswarm;

import com.capstone.javabox.csit228.games.JavaboxAbstractController;
import com.capstone.javabox.csit228.database.WindowSwarmDAO;
import com.capstone.javabox.csit228.utils.SoundManager;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WindowSwarmController extends JavaboxAbstractController {

    @FXML private Label skull;
    @FXML private Label scoreLabel;
    @FXML private Label warningLabel;

    private int currentScore = 0;
    private int activeWindows = 0;
    private final List<Stage> openSwarmWindows = new ArrayList<>();

    private final int MAX_WINDOWS = 10;
    private double currentSpawnRate = 2000;

    // --- CONSTANTS FOR EASY UI TWEAKING ---
    private static final double POPUP_WIDTH = 400;
    private static final double TEXT_POPUP_HEIGHT = 150;
    private static final double ACTION_POPUP_HEIGHT = 150;

    private Timeline spawnTimer;
    private Timeline glitchTimer;
    private final Random random = new Random();

    // --- CACHED FXML URLs (Prevents disk lookup lag during gameplay) ---
    private URL swarmUrl;
    private URL fatalErrorUrl;
    private URL restorePointUrl;

    private DropShadow cyanGlitch;
    private DropShadow redGlitch;

    private final String[] spamMessages = {
            "BUY NOW FOR ONLY 1 PESO!",
            "HOT SINGLE MOMS NEAR YOU!",
            "YOU WIN AN IPHONE! CLICK HERE!",
            "WE'RE INVADING YOU! LET US IN!",
            "YOUR PC IS INFECTED. DOWNLOAD ANTIVIRUS NOW.",
            "FREE ROBUX NO SCAM 2026",
            "ENLARGE YOUR MOUSE CURSOR TODAY!",
            "HAVE YOU HEARD OF [HYPERLINK BLOCKED] ?",
            "LOREM IPSUM DOLOR SIT AMET",
            "PLAY TUNIC!",
            "PLAY OUTER WILDS!",
            "NO BEACHES? BOOK AN ISLAND NOW!",
            "HOTEL? TRIVAGO!",
            "DO THIS TO REDUCE CANCER!",
            "AVAIL YOUR FREE 1000 PESO GIFT CARD HERE!",
            "I LOVE YOU! CLICK HERE TO SPREAD LOVE!",
            "CLICK HERE FOR MORE FEMBOYS!!!",
            "PLAY INDIE GAMES PLEASE PLEASE PLEASE!!!"
    };

    private final String[] titleMessages = {
            "IMPORTANT_MESSAGE.exe",
            "ATTENTION!",
            "WINDOW SWARM WINDOW SWARM!",
            "WIDOW SWARM!?!?",
            "PIRATES ARE APPROACHING FROM THE EAST!",
            "MADE BY COCO",
            "DID YOU KNOW...",
            "PLEASE ANSWER!",
            "WINDOW 67"
    };

    private final String hintText =
            "FATAL ERROR OCCURED!\n\n" +
            "VIRUS DETECTED! INCOMING MESSAGE:\n\n" +
            "'CONQUERING THIS DEVICE IS IMMINENT.'\n\n" +
            "MALWARE DEFENDER: Close windows to defend your device!";

    @FXML
    public void initialize() {
        SoundManager.playMusic("music_breakbeat.mp3");
        // Cache URLs at startup to keep the game loop lightning fast
        swarmUrl = getClass().getResource("swarm-window.fxml");
        fatalErrorUrl = getClass().getResource("fatal-error.fxml");
        restorePointUrl = getClass().getResource("restore-point.fxml");

        scoreLabel.setVisible(false);
        warningLabel.setVisible(false);
        skull.setOpacity(1.0);
        skull.setStyle("-fx-text-fill: #00FF41;");

        startGlitchEffect();

        Platform.runLater(() -> {
            Stage mainStage = (Stage) scoreLabel.getScene().getWindow();
            mainStage.setAlwaysOnTop(true);
            mainStage.setOnCloseRequest(Event::consume);
            spawnIntroSequence(mainStage);
        });
    }

    private void startGlitchEffect() {
        cyanGlitch = new DropShadow(0, -3, 0, Color.CYAN);
        redGlitch = new DropShadow(0, 3, 0, Color.RED);
        redGlitch.setInput(cyanGlitch);
        skull.setEffect(redGlitch);

        glitchTimer = new Timeline(new KeyFrame(Duration.millis(50), event -> {
            cyanGlitch.setOffsetX((random.nextDouble() - 0.5) * 8);
            cyanGlitch.setOffsetY((random.nextDouble() - 0.5) * 4);
            redGlitch.setOffsetX((random.nextDouble() - 0.5) * 8);
            redGlitch.setOffsetY((random.nextDouble() - 0.5) * 4);
            skull.setTranslateX((random.nextDouble() - 0.5) * 2);
        }));

        glitchTimer.setCycleCount(Timeline.INDEFINITE);
        glitchTimer.play();
    }

    private void spawnIntroSequence(Stage owner) {
        Stage introStage = new Stage();
        introStage.initOwner(owner);
        introStage.initModality(Modality.WINDOW_MODAL);
        introStage.initStyle(StageStyle.UTILITY);

        Label typeLabel = new Label();
        typeLabel.setStyle("-fx-text-fill: #00FF41; -fx-font-family: 'Courier New'; -fx-font-size: 16px; -fx-font-weight: bold;");
        typeLabel.setWrapText(true);
        typeLabel.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(typeLabel);
        root.setStyle("-fx-background-color: #000000; -fx-border-color: #00FF41; -fx-border-width: 2px;");
        root.setPadding(new Insets(20));

        introStage.setScene(new Scene(root, 650, 200));
        introStage.setTitle("WARNING.exe");
        introStage.setAlwaysOnTop(true);

        introStage.setY(0);

        String targetText = hintText;
        Timeline typingTimeline = new Timeline(new KeyFrame(Duration.millis(35), e -> {
            int currentLength = typeLabel.getText().length();
            if (currentLength < targetText.length()) {
                typeLabel.setText(targetText.substring(0, currentLength + 1));
            }
        }));
        typingTimeline.setCycleCount(targetText.length());
        typingTimeline.play();

        introStage.setOnCloseRequest(e -> {
            typingTimeline.stop();
            startActualGame();
        });

        introStage.show();
    }

    private void startActualGame() {
        FadeTransition fade = new FadeTransition(Duration.seconds(1.5), skull);
        fade.setToValue(0.2);
        fade.play();

        scoreLabel.setVisible(true);
        warningLabel.setVisible(true);
        updateUI();
        startGameLoop();
    }

    private void startGameLoop() {
        spawnTimer = new Timeline(new KeyFrame(Duration.millis(currentSpawnRate), event -> {
            spawnSwarmWindow();
            if (activeWindows >= MAX_WINDOWS) {
                triggerGameOver();
            }
        }));
        spawnTimer.setCycleCount(Timeline.INDEFINITE);
        spawnTimer.play();
    }

    private void spawnSwarmWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(swarmUrl);
            Parent root = loader.load();
            SwarmWindowController controller = loader.getController();

            controller.setSpamText(spamMessages[random.nextInt(spamMessages.length)]);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double randomWidth = 200 + random.nextDouble() * 400;
            double randomHeight = 150 + random.nextDouble() * 250;
            double randomX = screenBounds.getMinX() + (random.nextDouble() * (screenBounds.getWidth() - randomWidth));
            double randomY = screenBounds.getMinY() + (random.nextDouble() * (screenBounds.getHeight() - randomHeight));

            Stage swarmStage = new Stage();
            swarmStage.initOwner(scoreLabel.getScene().getWindow());
            swarmStage.setScene(new Scene(root));
            openSwarmWindows.add(swarmStage);

            swarmStage.setWidth(randomWidth);
            swarmStage.setHeight(randomHeight);
            swarmStage.setX(randomX);
            swarmStage.setY(randomY);
            swarmStage.setTitle(titleMessages[random.nextInt(titleMessages.length)]);
            swarmStage.setAlwaysOnTop(true);

            swarmStage.setOnCloseRequest(e -> {
                SoundManager.playSFX("sfx_explosion.mp3");
                currentScore++;
                activeWindows--;
                openSwarmWindows.remove(swarmStage);

                //Scaling logic
                if (currentScore % 5 == 0) {
                    SoundManager.playSFX("sfx_powerup.mp3");
                    if (currentScore < 50 && currentSpawnRate > 500) {
                        currentSpawnRate -= 100; // Phase 1: Fast scale
                    } else if (currentScore >= 50 && currentSpawnRate > 250) {
                        currentSpawnRate -= 50;  // Phase 2: Breathing room
                    }
                    spawnTimer.stop();
                    startGameLoop();
                }

                updateUI();
            });

            activeWindows++;
            updateUI();
            swarmStage.show();
            swarmStage.toFront();
            swarmStage.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load the Swarm Window FXML!");
        }
    }

    private void updateUI() {
        scoreLabel.setText("DATA RECOVERED: " + currentScore);
        warningLabel.setText("INFECTION LEVEL: " + activeWindows + "/" + MAX_WINDOWS);
    }

    private void triggerGameOver() {
        SoundManager.playSFX("sfx_death.mp3");
        spawnTimer.stop();

        scoreLabel.setVisible(false);
        warningLabel.setVisible(false);
        skull.setStyle("-fx-text-fill: #FF0000;");

        FadeTransition fade = new FadeTransition(Duration.seconds(2), skull);
        fade.setToValue(1.0);
        fade.play();

        for (Stage window : openSwarmWindows) {
            window.close();
        }
        openSwarmWindows.clear();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        List<Rectangle2D> occupiedSpaces = new ArrayList<>();

        Rectangle2D bounds1 = generateSafeCoordinates(screenBounds, POPUP_WIDTH, TEXT_POPUP_HEIGHT, occupiedSpaces);
        occupiedSpaces.add(bounds1);

        Rectangle2D bounds2 = generateSafeCoordinates(screenBounds, POPUP_WIDTH, TEXT_POPUP_HEIGHT, occupiedSpaces);
        occupiedSpaces.add(bounds2);

        Rectangle2D bounds3 = generateSafeCoordinates(screenBounds, POPUP_WIDTH, ACTION_POPUP_HEIGHT, occupiedSpaces);
        occupiedSpaces.add(bounds3);

        Stage overlayStage = new Stage();
        overlayStage.initStyle(StageStyle.TRANSPARENT);
        overlayStage.setAlwaysOnTop(true);
        overlayStage.setX(screenBounds.getMinX());
        overlayStage.setY(screenBounds.getMinY());
        overlayStage.setWidth(screenBounds.getWidth());
        overlayStage.setHeight(screenBounds.getHeight());

        Pane rootPane = new Pane();
        rootPane.setStyle("-fx-background-color: transparent;");

        Parent popup1 = createTextNode("SYSTEM FAILURE. VIRUS TAKEOVER COMPLETE", bounds1.getMinX(), bounds1.getMinY());
        Parent popup2 = createTextNode("YOU HAVE RECOVERED " + currentScore + " DATA", bounds2.getMinX(), bounds2.getMinY());
        Parent popup3 = createActionNode(overlayStage, bounds3.getMinX(), bounds3.getMinY());

        if (popup1 != null) rootPane.getChildren().add(popup1);
        if (popup2 != null) rootPane.getChildren().add(popup2);
        if (popup3 != null) rootPane.getChildren().add(popup3);

        Scene scene = new Scene(rootPane);
        scene.setFill(Color.TRANSPARENT);
        overlayStage.setScene(scene);
        overlayStage.setOnHidden(e -> rootPane.getChildren().clear());

        overlayStage.show();
    }

    private Parent createTextNode(String message, double x, double y) {
        try {
            FXMLLoader loader = new FXMLLoader(fatalErrorUrl);
            Parent root = loader.load();

            Label messageLabel = (Label) loader.getNamespace().get("messageLabel");
            messageLabel.setText(message);

            root.setLayoutX(x);
            root.setLayoutY(y);

            makeDraggable(root);
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Parent createActionNode(Stage overlayStage, double x, double y) {
        try {
            FXMLLoader loader = new FXMLLoader(restorePointUrl);
            Parent root = loader.load();

            Button rebootBtn = (Button) loader.getNamespace().get("rebootBtn");
            Button lobbyBtn = (Button) loader.getNamespace().get("lobbyBtn");

            root.setLayoutX(x);
            root.setLayoutY(y);

            rebootBtn.setOnAction(e -> {
                overlayStage.close();
                restartGame();
            });

            lobbyBtn.setOnAction(e -> {
                SoundManager.playMusic(true, "music_lobby_music1.mp3", "music_lobby_music2.mp3");
                overlayStage.close();
                Stage mainStage = (Stage) scoreLabel.getScene().getWindow();
                mainStage.setAlwaysOnTop(false);
                mainStage.setOnCloseRequest(null);
                quitToLobby();
            });

            makeDraggable(root);
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void restartGame() {
        currentScore = 0;
        activeWindows = 0;
        currentSpawnRate = 2000;

        skull.setStyle("-fx-text-fill: #00FF41;");
        skull.setOpacity(0.2);
        scoreLabel.setVisible(true);
        warningLabel.setVisible(true);

        updateUI();

        Stage mainStage = (Stage) scoreLabel.getScene().getWindow();
        mainStage.setOnCloseRequest(Event::consume);

        startGameLoop();
    }

    private void makeDraggable(Node node) {
        final double[] dragDelta = new double[2];

        node.setOnMousePressed(e -> {
            dragDelta[0] = node.getLayoutX() - e.getSceneX();
            dragDelta[1] = node.getLayoutY() - e.getSceneY();
            node.toFront();
        });

        node.setOnMouseDragged(e -> {
            node.setLayoutX(e.getSceneX() + dragDelta[0]);
            node.setLayoutY(e.getSceneY() + dragDelta[1]);
        });
    }

    private Rectangle2D generateSafeCoordinates(Rectangle2D screen, double width, double height, List<Rectangle2D> placedWindows) {
        int maxAttempts = 100;

        for (int i = 0; i < maxAttempts; i++) {
            double randomX = screen.getMinX() + (random.nextDouble() * (screen.getWidth() - width));
            double randomY = screen.getMinY() + (random.nextDouble() * (screen.getHeight() - height));

            Rectangle2D candidate = new Rectangle2D(randomX, randomY, width, height);
            boolean collision = false;

            for (Rectangle2D placed : placedWindows) {
                Rectangle2D paddedPlaced = new Rectangle2D(
                        placed.getMinX() - 20, placed.getMinY() - 20,
                        placed.getWidth() + 40, placed.getHeight() + 40
                );

                if (candidate.intersects(paddedPlaced)) {
                    collision = true;
                    break;
                }
            }

            if (!collision) {
                return candidate;
            }
        }
        return new Rectangle2D(screen.getMinX(), screen.getMinY(), width, height);
    }

    private Parent createActionNode(Stage overlayStage, double x, double y) {
        try {
            FXMLLoader loader = new FXMLLoader(restorePointUrl);
            Parent root = loader.load();

            Button rebootBtn = (Button) loader.getNamespace().get("rebootBtn");
            Button lobbyBtn = (Button) loader.getNamespace().get("lobbyBtn");
            TextField playerNameInput = (TextField) loader.getNamespace().get("playerNameInput");
            Button saveBtn = (Button) loader.getNamespace().get("saveBtn");

            root.setLayoutX(x);
            root.setLayoutY(y);

            // --- NEW SAVE LOGIC ---
            saveBtn.setOnAction(e -> {
                String alias = playerNameInput.getText().trim();
                if (!alias.isEmpty()) {
                    // Send it to the database!
                    WindowSwarmDAO.saveScore(alias, currentScore);

                    // Disable the inputs so they can't spam save
                    saveBtn.setText("UPLOADED!");
                    saveBtn.setDisable(true);
                    playerNameInput.setDisable(true);
                }
            });

            rebootBtn.setOnAction(e -> {
                overlayStage.close();
                restartGame();
            });

            lobbyBtn.setOnAction(e -> {
                SoundManager.stopMusic();
                overlayStage.close();
                Stage mainStage = (Stage) scoreLabel.getScene().getWindow();
                mainStage.setAlwaysOnTop(false);
                mainStage.setOnCloseRequest(null);
                quitToLobby();
            });

            makeDraggable(root);
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}