package com.capstone.javabox.csit228.games.bookwormbattle;

import com.capstone.javabox.csit228.games.JavaboxGame;
import javafx.stage.Stage;

public class BookwormBattleLauncher implements JavaboxGame {

    @Override
    public String getGameTitle() {
        return "Bookworm Battle";
    }


    @Override
    public void launchGame(Stage activeStage, Runnable onQuit) {
        loadAndLaunch(activeStage, onQuit, "Bookworm Battle", "bookwormbattle-menu.fxml", 1100, 850);
    }
}