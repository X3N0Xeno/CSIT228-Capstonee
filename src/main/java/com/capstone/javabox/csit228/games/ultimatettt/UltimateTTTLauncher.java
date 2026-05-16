package com.capstone.javabox.csit228.games.ultimatettt;

import com.capstone.javabox.csit228.games.JavaboxGame;
import javafx.stage.Stage;

public class UltimateTTTLauncher implements JavaboxGame {

    @Override
    public String getGameTitle() {
        return "Ultimate TTT";
    }

    @Override
    public void launchGame(Stage activeStage, Runnable onQuit) {
        loadAndLaunch(activeStage, onQuit, "Ultimate TTT", "ultimate-ttt-view.fxml", 900, 800);
    }
}