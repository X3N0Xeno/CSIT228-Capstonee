package com.capstone.javabox.csit228.games.knucklebones;

import com.capstone.javabox.csit228.games.JavaboxGame;
import javafx.stage.Stage;

public class KnuckleBoneLauncher implements JavaboxGame {

    @Override
    public String getGameTitle() {
        return "Knucklebones";
    }

    @Override
    public void launchGame(Stage activeStage, Runnable onQuit) {
        loadAndLaunch(activeStage, onQuit, "Knucklebones - Menu", "knucklebone-menu.fxml", 1280, 720);
    }
}