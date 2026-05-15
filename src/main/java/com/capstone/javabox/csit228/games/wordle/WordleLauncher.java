package com.capstone.javabox.csit228.games.wordle;

import com.capstone.javabox.csit228.games.JavaboxGame;
import javafx.application.Application;
import javafx.stage.Stage;

public class WordleLauncher implements JavaboxGame {
    @Override
    public String getGameTitle() {
        return "Wordle++";
    }

    @Override
    public void launchGame(Stage activeStage, Runnable onQuit) {
        loadAndLaunch(activeStage, onQuit, "Wordle++", "wordle-view.fxml", 620, 700);
    }
}

