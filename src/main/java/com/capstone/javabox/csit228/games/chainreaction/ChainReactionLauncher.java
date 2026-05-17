package com.capstone.javabox.csit228.games.chainreaction;

import com.capstone.javabox.csit228.games.JavaboxGame;
import javafx.stage.Stage;

public class ChainReactionLauncher implements JavaboxGame {

    @Override
    public String getGameTitle() {
        return "Chain Reaction";
    }

    @Override
    public void launchGame(Stage activeStage, Runnable onQuit) {
        loadAndLaunch(activeStage, onQuit, "Chain Reaction", "chain-reaction-view.fxml", 900, 800);
    }
}