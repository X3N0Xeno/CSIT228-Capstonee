package com.capstone.javabox.csit228.games.hangman;

import com.capstone.javabox.csit228.games.JavaboxGame;
import javafx.stage.Stage;

public class HangmanLauncher implements JavaboxGame {
    @Override
    public String getGameTitle() {
        return "Hangman Roulette";
    }

    @Override
    public void launchGame(Stage activeStage, Runnable onQuit) {
        loadAndLaunch(activeStage, onQuit, "Hangman x Russian Roulette", "hangman-view.fxml", 620, 700);
    }
}
