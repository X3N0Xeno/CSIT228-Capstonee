package com.capstone.javabox.csit228.games.gemforge;

import com.capstone.javabox.csit228.games.JavaboxGame;
import javafx.stage.Stage;

public class GemForgeLauncher implements JavaboxGame {

    @Override
    public String getGameTitle() {
        return "Gem Forge";
    }

    @Override
    public void launchGame(Stage activeStage, Runnable onQuit) {
        loadAndLaunch(activeStage, onQuit, "Gem Forge","gemforge-main.fxml", 1000, 800);
    }
}
