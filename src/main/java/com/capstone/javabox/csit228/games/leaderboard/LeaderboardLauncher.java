package com.capstone.javabox.csit228.games.leaderboard;

import com.capstone.javabox.csit228.games.JavaboxGame;
import javafx.stage.Stage;

public class LeaderboardLauncher implements JavaboxGame {

    @Override
    public String getGameTitle() {
        return "Hall of Fame";
    }

    @Override
    public void launchGame(Stage activeStage, Runnable onQuit) {
        loadAndLaunch(activeStage, onQuit, "JavaBox Hall of Fame", "leaderboard-view.fxml", 1280, 720);
    }
}