package com.capstone.javabox.csit228.games.fullhouse;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.List;
import java.util.function.Consumer;

public class FullHouseEngine {
    private List<House> racingHouses;
    private Runnable updateUI;
    private Consumer<House> onWin;
    private Timeline gameLoop;
    private final double FINISH_LINE = 70.0; // X-coordinate for the win

    public FullHouseEngine(List<House> houses, Runnable updateUI, Consumer<House> onWin) {
        this.racingHouses = houses;
        this.updateUI = updateUI;
        this.onWin = onWin;
    }

    public void startRace() {
        gameLoop = new Timeline(new KeyFrame(Duration.millis(150), e -> {
            for (House h : racingHouses) {
                h.takeTurn();
                if (h.getPosition() >= FINISH_LINE) {
                    gameLoop.stop();
                    onWin.accept(h);
                    break;
                }
            }
            updateUI.run();
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }
}