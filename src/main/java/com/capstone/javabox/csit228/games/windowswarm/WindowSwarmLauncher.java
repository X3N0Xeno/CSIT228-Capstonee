package com.capstone.javabox.csit228.games.windowswarm;

import com.capstone.javabox.csit228.games.JavaboxGame;
import javafx.stage.Stage;

public class WindowSwarmLauncher implements JavaboxGame {
    @Override
    public String getGameTitle() {
        return "Window Swarm";
    }

    @Override
    public void launchGame(Stage activeStage, Runnable onQuit) {
        loadAndLaunch(activeStage, onQuit, "Window Swarm", "windowswarm-main.fxml", 854, 480);
    }
}