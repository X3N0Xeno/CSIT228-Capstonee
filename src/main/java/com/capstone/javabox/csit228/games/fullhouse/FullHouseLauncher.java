package com.capstone.javabox.csit228.games.fullhouse;

import com.capstone.javabox.csit228.games.JavaboxGame;
import javafx.stage.Stage;

public class FullHouseLauncher implements JavaboxGame {

    @Override
    public String getGameTitle() {
        return "Full House";
    }

    @Override
    public void launchGame(Stage activeStage, Runnable onQuit) {
        // Uses your interface's default method.
        // Looks for "fullhouse-view.fxml" in the SAME package inside resources.
        loadAndLaunch(activeStage, onQuit, "Full House: Estate Race", "fullhouse-view.fxml", 1280, 720);
    }
}